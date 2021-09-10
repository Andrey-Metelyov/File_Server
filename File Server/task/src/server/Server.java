package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final String address;
    private final int port;
    ServerSocket server;
    Socket socket = null;
    DataInputStream input = null;
    DataOutputStream output = null;

    Server(String address, int port) {
        this.address = address;
        this.port = port;
    }

    boolean start() {
        try {
            server = new ServerSocket(port, 50, InetAddress.getByName(address));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Server started!");
        return true;
    }

    boolean accept() {
        try {
            socket = server.accept();
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    void shutdown() {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String read() {
        try {
            return input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void send(String string) {
        try {
            output.writeUTF(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
