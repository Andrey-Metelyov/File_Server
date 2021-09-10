package server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    static String rootDir = System.getProperty("user.dir") +
            File.separator + "." +
            File.separator + "src" +
            File.separator + "server" +
            File.separator + "data" +
            File.separator;

    public static void main(String[] args) {
        String address = "127.0.0.1";
        int port = 23456;
        FileServer server = new FileServer(address, port, rootDir);
        try {
            Path path = Files.createDirectories(Path.of(rootDir));
            System.err.println("Directory " + path.toAbsolutePath() + " created");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (server.start()) {
            while (server.isRunning()) {
                server.accept();
            }
        }
    }
}
