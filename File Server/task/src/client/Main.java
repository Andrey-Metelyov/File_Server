package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        System.out.println("Client started!");
        String address = "127.0.0.1";
        int port = 23456;
        try (Socket socket = new Socket(InetAddress.getByName(address), port);
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            String command = "Give me everything you have!";
            output.writeUTF(command);
            System.out.printf("Sent: %s\n", command);

            String answer = input.readUTF();
            System.out.printf("Received: %s\n", answer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
