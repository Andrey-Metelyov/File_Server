package client;

import common.Request;
import common.Response;
import common.Session;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    static String homeDir = System.getProperty("user.dir") +
            File.separator + "." +
            File.separator + "src" +
            File.separator + "client" +
            File.separator + "data" +
            File.separator;

    public static void main(String[] args) {
        try {
            Path path = Files.createDirectories(Path.of(homeDir));
            System.err.println("Directory " + path.toAbsolutePath() + " created");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Client started!");
        String address = "127.0.0.1";
        int port = 23456;
        try (Session session = new Session(
                new Socket(InetAddress.getByName(address), port))) {
            System.out.println("Enter action (1 - get a file, 2 - create a file, 3 - delete a file):");
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();
            switch (command) {
                case "1": { // get
                    System.out.print("Do you want to get the file by name or by id (1 - name, 2 - id): ");
                    String answer = scanner.nextLine();
                    if (answer.equals("1")) { // name
                        System.out.println("Enter name: ");
                        String filename = scanner.nextLine();
                        session.sendRequest(
                                new Request("GET",
                                        List.of("BY_NAME", filename)));
                    } else {
                        System.out.println("Enter id: ");
                        String id = scanner.nextLine();
                        session.sendRequest(
                                new Request("GET",
                                        List.of("BY_ID", id)));
                    }
                    System.out.println("The request was sent.");
                    Response resp = session.getResponse();
                    if (resp == null) {
                        System.out.println("No response...");
                    } else if (resp.id == 200) {
                        byte[] message = session.getFile();
                        System.out.print("The file was downloaded! Specify a name for it: ");
                        String filename = scanner.nextLine();
                        Files.write(Paths.get(homeDir, filename), message);
                    } else {
                        System.out.println("The response says that this file is not found!");
                    }
                    break;
                }
                case "2": { // put
                    System.out.print("Enter name of the file: ");
                    String filename = scanner.nextLine();
                    System.out.print("Enter name of the file to be saved on server: ");
                    String serverFilename = scanner.nextLine();
                    session.sendRequest(
                            new Request("PUT",
                                    List.of(serverFilename)));
                    session.sendFile(new File(homeDir, filename));
                    Response resp = session.getResponse();
                    if (resp == null) {
                        System.out.println("No response...");
                    } else if (resp.id == 200) {
                        System.out.printf("Response says that file is saved! ID = %s\n", resp.content);
                    } else {
                        System.out.println("The response says that this file is not found!");
                    }
                    break;
                }
                case "3": { // delete
                    System.out.print("Do you want to delete the file by name or by id (1 - name, 2 - id): ");
                    String answer = scanner.nextLine();
                    Request request;
                    if (answer.equals("1")) { // name
                        System.out.println("Enter name: ");
                        String filename = scanner.nextLine();
                        request = new Request("DELETE", List.of("BY_NAME", filename));
                    } else if (answer.equals("2")) {
                        System.out.println("Enter id: ");
                        String id = scanner.nextLine();
                        request = new Request("DELETE", List.of("BY_ID", id));
                    } else {
                        break;
                    }
                    session.sendRequest(request);
                    System.out.println("The request was sent.");
                    Response resp = session.getResponse();
                    if (resp == null) {
                        System.out.println("No response...");
                    } else if (resp.id == 200) {
                        System.out.println("The response says that this file was deleted successfully!");
                    } else {
                        System.out.println("The response says that this file is not found!");
                    }
                    break;
                }
                case "exit":
                    session.send("exit");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private static byte[] getFile(DataInputStream input) throws IOException {
//        int length = input.readInt();
//        byte[] message = new byte[length];
//        input.readFully(message,0,length);
//        return message;
//    }

//    private static void sendFile(String filename, DataOutputStream output) {
//        try {
//            byte[] message = Files.readAllBytes(Path.of(filename));
//            output.writeInt(message.length);
//            output.write(message);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static Response getResponse(DataInputStream input) {
//        try {
//            String[] response = input.readUTF().split(" ");
//            return new Response(Integer.parseInt(response[0]), response[1]);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}
