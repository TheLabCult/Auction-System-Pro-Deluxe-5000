package com.auction.server.network;


import com.auction.server.dao.impl.*; 


import com.auction.server.controllers.*;

// JSON
import com.google.gson.Gson;
import com.google.gson.GsonBuilder; 

// Java networking và concurrency
import java.io.IOException;
import java.net.ServerSocket; // lắng nghe các kết nối TCP tới
import java.net.Socket;       // một client socket cho mỗi một kết nối tới
import java.util.concurrent.ExecutorService; // thread pool chạy các ClientHandler
import java.util.concurrent.Executors;       // factory cho thread pool implementations

public final class AuctionServer {

    private final int port; // cổng TCP để lắng nghe (default 9090, lấy từ ServerMain)

    private final Gson gson;

    private final UserManager    userManager;
    private final ItemManager    ItemManager;
    private final AuctionManager auctionManager;
    private final BidManager     bidManager;

    private final ExecutorService clientPool =
            Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r);
                return t;
            });

    public AuctionServer(int port) {
        this.port = port;
        /* serializeNulls: Chấp nhận thuộc tính null như winnerId khi chưa có winner hoặc 
        đấu giá kết thúc mà không có winner */
        this.gson = new GsonBuilder().serializeNulls().create();

        /*Khởi tạo DAO, dùng để kết nối tới db */
        SQLiteUserDAO    userDAO    = new SQLiteUserDAO();
        SQLiteItemDAO    itemDAO    = new SQLiteItemDAO();
        SQLiteAuctionDAO auctionDAO = new SQLiteAuctionDAO();
        SQLiteBidDAO     bidDAO     = new SQLiteBidDAO();
        SQLiteAutoBidDAO autoBidDAO = new SQLiteAutoBidDAO();

        /* khởi tạo các đối tượng business logic, auctionManager trước bidManager vì bid cần gọi auction */
        userManager    = new UserManager(userDAO);
        ItemManager    = new ItemManager(itemDAO);
        auctionManager = new AuctionManager(auctionDAO); // also restores scheduler on startup
        bidManager     = new BidManager(auctionDAO, bidDAO, autoBidDAO, auctionManager);
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            
            // lặp đến khi thread bị ngắt (ctrl C)
            while (!Thread.currentThread().isInterrupted()) {
                Socket client = serverSocket.accept(); 
                /* tạo một ClientHandler cho kết nối này và submit nó vào trong pool */
                /* pool chạy ClientHandler.run() */
                clientPool.submit(new ClientHandler(
                        client, gson,
                        userManager, ItemManager, auctionManager, bidManager));
            }
        }
    }
}
