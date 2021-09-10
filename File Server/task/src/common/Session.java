package common;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class Session implements Closeable {
    Socket socket;
    DataInputStream input = null;
    DataOutputStream output = null;

    public Session(Socket socket) {
        this.socket = socket;
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
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

    public byte[] getFile() {
        System.err.println("getFile");
        try {
            int length = input.readInt();
            System.err.println("length: " + length);
            byte[] message = new byte[length];
            input.readFully(message,0,length);
            return message;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendFile(File file) {
        System.err.println("Send file " + file.getAbsolutePath());
        try {
            byte[] message = Files.readAllBytes(file.toPath());
            output.writeInt(message.length);
            System.err.println("length: " + message.length);
            output.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendResponse(Response response) {
        System.err.println("sendResponse(" + response + ")");
        try {
            output.writeUTF(response.id + " " + response.content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        if (input != null) {
            input.close();
        }
        if (output != null) {
            output.close();
        }
    }

    public void sendRequest(Request request) {
        try {
            output.writeUTF(request.command + " " +
                    String.join(" ", request.parameters));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Response getResponse() {
        try {
            String[] response = input.readUTF().split(" ");
            return new Response(Integer.parseInt(response[0]), response.length > 1 ? response[1] : "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
