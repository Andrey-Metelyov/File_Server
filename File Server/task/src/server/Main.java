package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        System.out.println("Server started!");
        String address = "127.0.0.1";
        int port = 23456;
        try (
            ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address));
            Socket socket = server.accept();
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output  = new DataOutputStream(socket.getOutputStream()))
        {
            String command = input.readUTF();
            if (command.equals("Give me everything you have!")) {
                System.out.printf("Received: %s\n", command);
                String answer = "All files were sent!";
                output.writeUTF(answer);
                System.out.printf("Sent: %s\n", answer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}