package com.auction.server.network;
//Đây là nơi Server thực sự đọc gói tin Request, gọi logic từ AuctionManager, và trả về Response
import com.auction.server.controllers.*;
import com.auction.server.observer.AuctionObserver;
import com.auction.shared.models.*;
import com.auction.shared.network.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable, AuctionObserver {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private AuctionManager auctionManager;
    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.auctionManager = AuctionManager.getInstance(); //bộ điều phối trung tâm
    }
    @Override
    public void run() {
        try {
            //luon tao ObjectOutputStream truoc ObjectInputStream
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                //doc request tu client (qua trinh Deserialize)
                Request request = (Request) in.readObject();
                Response response = processRequest(request);
                //gui response tra lai client (qua trinh Serialize)
                out.writeObject(response);
                out.flush();
            }
        }
        catch (Exception e) {
            System.out.println("Client Disconnected: " + socket.getInetAddress());
        }
        finally {
            closeConnections();
        }
    }
    private Response processRequest(Request request) {
        try {
            switch (request.getAction()) {
                case "GET_ACTIVE_AUCTIONS":
                    List<Auction> activeAuctions = auctionManager.getActiveAuctions();
                    return new Response("SUCCESS", "Get active auctions", activeAuctions);

                case "PLACE_BID":
                    //ep kieu payload thanh mang gom [auctionId, BidTransaction]
                    Object[] payloadData = (Object[]) request.getPayload();
                    String auctionId = (String) payloadData[0];
                    BidTransaction newBid = (BidTransaction) payloadData[1];

                    Auction targetAuction = auctionManager.getAuctionById(auctionId);
                    if (targetAuction != null) {
                        targetAuction.placeBid(newBid);
                        //TODO: luu vao database
                        return new Response("SUCCESS", "Place bid for auction", targetAuction);
                    } else return new Response("ERROR", "Auction not found", targetAuction);

                default:
                    return new Response("ERROR", "Invalid action", null);
                }
            }
        catch (Exception e) {
            return new Response("ERROR", e.getMessage(), null);
        }
    }

    private void closeConnections() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //ham gui du lieu qua socket phai dc khoa (synchronized)
    private synchronized void sendResponse(Response response) {
        try {
            if (out != null) {
                out.writeObject(response);
                out.reset();
                out.flush();
            }
        }
        catch (IOException e) {}
    }
    //cac ham cua observer
    @Override
    public void onNewBid(String auctionId, BidTransaction newBid) {
        //dong goi thanh response mang flag "REALTIME_UPDATE"
        Object[] payload = new Object[]{auctionId, newBid};
        Response response = new Response("REALTIME_UPDATE", "NEW_BID", payload);
        //gui qua mang
        sendResponse(response);
    }
    @Override
    public void onAuctionEnded(String auctionId, BidTransaction winningBid) {
        Object[] payload = new Object[]{auctionId, winningBid};
        Response response = new Response("REALTIME_UPDATE", "AUCTION_ENDED", payload);
        sendResponse(response);
    }
}
