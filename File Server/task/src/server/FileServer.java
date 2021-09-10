package server;

import common.Request;
import common.Response;
import common.Session;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class FileServer {
    private final String SERVER_CONFIG = "server.conf";

    private final String address;
    private final int port;
    private final String rootDir;
    private boolean running = false;
    private final Map<Integer, String> files = new HashMap<>();
    private ServerSocket server;
    private ExecutorService executor;

    FileServer(String address, int port, String rootDir) {
        this.address = address;
        this.port = port;
        this.rootDir = rootDir;
    }

    boolean start() {
        try {
            loadFiles();
            server = new ServerSocket(port, 50, InetAddress.getByName(address));
            running = true;
            int processors = Runtime.getRuntime().availableProcessors();
            executor = Executors.newFixedThreadPool(processors);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Server started!");
        return true;
    }

    private void loadFiles() {
        try {
            files.putAll(
                    Files.lines(Path.of(SERVER_CONFIG))
                            .collect(
                                    Collectors.toMap(
                                            it -> Integer.valueOf(it.split("=")[0]),
                                            it -> it.split("=")[1]
                                    )
                            )
            );
            System.err.println("loadFiles: " + files);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFiles() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, String> entry : files.entrySet()) {
            sb.append(entry.getKey()).append('=').append(entry.getValue()).append(System.lineSeparator());
        }
        try {
            System.err.println("saveFiles: " + sb);
            Files.writeString(Path.of(SERVER_CONFIG), sb);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void accept() {
        System.err.println("waiting connection...");
        try {
            Socket socket = server.accept();
            executor.submit(new ServerRunnable(this, new Session(socket)));
        } catch (IOException e) {
            System.err.println("exception in accept()");
            e.printStackTrace(System.err);
        }
    }

    synchronized void shutdown() {
        System.err.println("shutdown...");
        try {
            running = false;
            saveFiles();
            server.close();
            executor.shutdown();
        } catch (IOException e) {
            System.err.println("exception in shutdown");
            e.printStackTrace();
        }
    }

    synchronized public boolean isRunning() {
        return running;
    }

    synchronized public int registerFile(File file) {
        int id = (int) (System.currentTimeMillis() & 0x00FF_FFFF);
        files.put(id, file.getName());
        System.err.println("registerFile(" + file + ") = " + id);
        return id;
    }

    synchronized private boolean deleteFile(String filename) {
        try {
            Files.delete(Path.of(rootDir, filename));
            System.err.println(files);
            files.values().remove(filename);
            System.err.println(files);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    synchronized private File createFile(String filename, byte[] content) {
        System.err.println("createFile(" + filename + ")");
        try {
            if (Files.exists(Paths.get(rootDir, filename))) {
                return null;
            }
            Path path = Files.write(Paths.get(rootDir, filename), content);
            return path.toFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    synchronized private File getFile(String filename) {
        return files.containsValue(filename) ? new File(rootDir, filename) : null;
//        System.err.println("getFile(" + filename + ")");
//        try {
//            Stream<Path> walk = Files.walk(Path.of(rootDir));
//            Optional<Path> file = walk.filter(f -> f.getFileName().toFile().getName().equals(filename)).findAny();
//            return file.map(Path::toFile).orElse(null);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
    }

    synchronized private String generateFilename() {
        return String.valueOf(System.currentTimeMillis());
    }

    private class ServerRunnable implements Runnable {
        FileServer server;
        Session session;

        public ServerRunnable(FileServer server, Session session) {
            this.server = server;
            this.session = session;
        }

        @Override
        public void run() {
            String strRequest = session.read();
            System.err.println("strRequest=" + strRequest);
            if (strRequest.equals("exit")) {
                System.err.println("send shutdown");
                server.shutdown();
                return;
            }
            String[] command = strRequest.split(" ", 2);
            Request request = new Request(command[0], List.of(command[1].split(" ")));
            System.err.println(request);
            switch (request.command) {
                case "GET": {
                    String type = request.parameters.get(0);
                    File file;
                    if (type.equals("BY_NAME")) {
                        String filename = request.parameters.get(1);
                        System.err.println("GET BY_NAME " + filename);
                        file = getFile(filename);
                    } else {
                        System.err.println(request.parameters);
                        try {
                            int id = Integer.parseInt(request.parameters.get(1));
                            System.err.println("GET BY_ID " + id);
                            file = getFile(files.get(id));
                        } catch (NumberFormatException e) {
                            file = null;
                        }
                    }
                    if (file != null) {
                        //                            String content = Files.readString(file.toPath());
                        session.sendResponse(new Response(200));
                        session.sendFile(file);
//                            session.send("The content of the file is: " + content);
                    } else {
//                        session.send("The response says that the file was not found!");
                        session.sendResponse(new Response(404));
                    }
                    break;
                }
                case "PUT": {
                    String filename = request.parameters.get(0);
                    if (filename.isEmpty()) {
                        filename = generateFilename();
                    }
                    byte[] fileContent = session.getFile();
                    File file = createFile(filename, fileContent);
                    System.err.println("File created " + file);
                    if (file != null) {
                        int id = server.registerFile(file);
                        System.err.println("id=" + id);
//                        session.send("The response says that the file was created!");
                        session.sendResponse(new Response(200, String.valueOf(id)));
                    } else {
                        session.sendResponse(new Response(403));
//                        session.send("The response says that creating the file was forbidden!");
                    }
                    break;
                }
                case "DELETE": {
                    String type = request.parameters.get(0);
                    String filename;
                    if (type.equals("BY_NAME")) {
                        filename = request.parameters.get(1);
                        System.err.println("DELETE BY_NAME " + filename);
                    } else {
                        System.err.println(request.parameters);
                        try {
                            int id = Integer.parseInt(request.parameters.get(1));
                            System.err.println("DELETE BY_ID " + id);
                            filename = files.get(id);
                        } catch (NumberFormatException e) {
                            filename = null;
                        }
                    }
                    if (filename != null && deleteFile(filename)) {
                        session.sendResponse(new Response(200));
                    } else {
                        session.sendResponse(new Response(404));
                    }
                    break;
                }
                default:
                    System.err.println("Unknown command " + command[0]);
                    break;
            }
        }
    }
}
