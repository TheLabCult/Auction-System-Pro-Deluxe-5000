package main.java.com.auction.client.network;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket server;
    private DataInputStream in;
    public static final int PORT = 7770;
    public static final String STOP_STRING = "##";

    public Server() {
        try {
            server = new ServerSocket(PORT);
            initConnection();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initConnection() throws IOException {
        Socket clientSocket = server.accept();
        in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
        readMessages();
    }

    public void close() throws IOException {
        in.close();
        server.close();
    }

    private void readMessages() throws IOException {
        String line = "";
        while (!line.equals(STOP_STRING)) {
            line = in.readUTF();
            System.out.println(line);
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}