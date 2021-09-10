package server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class Main {
    static String rootDir = "/server/data/";

    public static void main(String[] args) {
        String address = "127.0.0.1";
        int port = 23456;
        Server server = new Server(address, port);
        if (server.start()) {
            while (true) {
                server.accept();
                String response = server.read();
                if (response.equals("exit")) {
                    server.shutdown();
                    break;
                }
                String[] command = response.split(" ");
                switch (command[0]) {
                    case "GET" -> {
                        File file = getFile(command[1]);
                        if (file != null) {
                            try {
                                String content = Files.readString(file.toPath());
                                server.send("The content of the file is: " + content);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            server.send("The response says that the file was not found!");
                        }
                    }
                    case "PUT" -> {
                        File file = createFile(command[1], command[2]);
                        if (file != null) {
                            server.send("The response says that the file was created!");
                        } else {
                            server.send("The response says that creating the file was forbidden!");
                        }
                    }
                    case "DELETE" -> {
                        if (deleteFile(command[1])) {
                            server.send("The response says that the file was successfully deleted!");
                        } else {
                            server.send("The response says that the file was not found!");
                        }
                    }
                    default -> System.err.println("Unknown command " + command[0]);
                }
            }
        }
    }

    private static boolean deleteFile(String filename) {
        try {
            Files.delete(Path.of(rootDir, filename));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static File createFile(String filename, String content) {
        try {
            Path path = Files.writeString(Paths.get(rootDir, filename), content);
            return path.toFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static File getFile(String filename) {
        try {
            Stream<Path> walk = Files.walk(Path.of(rootDir));
            Optional<Path> file = walk.filter(f -> f.getFileName().toFile().getName().equals(filename)).findAny();
            return file.map(Path::toFile).orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
