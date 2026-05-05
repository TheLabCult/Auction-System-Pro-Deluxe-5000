package com.auction.server.network;

import com.auction.server.controllers.AuctionManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AuctionServer {
    private static final int PORT = 8080;
    public static void main(String[] args) {
        System.out.println("Auction Server starting...");
        AuctionManager manager = AuctionManager.getInstance();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port: " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected from IP: " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                (new Thread(clientHandler)).start();
            }
        }
        catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
        }
    }
}
