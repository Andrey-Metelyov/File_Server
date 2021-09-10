package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println("Client started!");
        String address = "127.0.0.1";
        int port = 23456;
        try (Socket socket = new Socket(InetAddress.getByName(address), port);
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            System.out.println("Enter action (1 - get a file, 2 - create a file, 3 - delete a file):");
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();
            switch (command) {
                case "1" -> {
                    System.out.print("Enter filename: ");
                    String filename = scanner.nextLine();
                    output.writeUTF("GET " + filename);
                }
                case "2" -> {
                    System.out.print("Enter filename: ");
                    String filename = scanner.nextLine();
                    System.out.print("Enter file content: ");
                    String content = scanner.nextLine();
                    output.writeUTF("PUT " + filename + " " + content);
                }
                case "3" -> {
                    System.out.print("Enter filename: ");
                    String filename = scanner.nextLine();
                    output.writeUTF("DELETE " + filename);
                }
            }
            String answer = input.readUTF();
            System.out.printf("Received: %s\n", answer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
