package com.auction.server.network;


import com.auction.shared.dto.*;    
import com.auction.shared.protocol.*;
import com.auction.server.enums.*;
import com.auction.shared.network.Requests.*;
import com.auction.shared.network.Responses.*;

import com.auction.server.exceptions.AuctionException;
import com.auction.server.exceptions.AuthException;
import com.auction.server.exceptions.BidException;

import com.auction.server.models.*;

import com.auction.server.observer.AuctionEventBus;
import com.auction.server.observer.AuctionObserver;

import com.auction.server.controllers.*;

import com.auction.server.util.DtoMapper;
import com.google.gson.Gson;             

// Java I/O
import java.io.*;       // BufferedReader, InputStreamReader, PrintWriter, IOException
import java.net.Socket; 


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import java.util.List;
import java.util.stream.Collectors; // stream().map().collect() for DTO list conversions


public final class ClientHandler implements Runnable, AuctionObserver {

    
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Socket socket;         // the TCP connection for this client
    private final Gson gson;             // shared (thread-safe) JSON serialiser
    private final UserManager userManager;
    private final ItemManager itemManager;
    private final AuctionManager auctionManager;
    private final BidManager bidManager;
    private final AuctionEventBus eventBus = AuctionEventBus.getInstance();

    private PrintWriter out;       // writes JSON lines to the socket; set in run()
    private User currentUser;      // null = not logged in; set on successful LOGIN

    public ClientHandler(Socket socket, Gson gson,
                         UserManager userManager, ItemManager itemManager,
                         AuctionManager auctionManager, BidManager bidManager) {
        this.socket         = socket;
        this.gson           = gson;
        this.userManager    = userManager;
        this.itemManager    = itemManager;
        this.auctionManager = auctionManager;
        this.bidManager     = bidManager;
    }

    // Main I/O loop

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true)
            // 'true' = auto-flush: every println() immediately sends the line
        ) {
            this.out = out;
            String line;
            while ((line = in.readLine()) != null) { // blocks until data or disconnect
                try {
                    Message msg = gson.fromJson(line, Message.class);
                    dispatch(msg);
                } catch (Exception e) {
                    sendError(null, "Malformed message: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            
        } finally {
            // Critical cleanup: remove this handler from ALL watcher sets.
            // Without this, the dead handler stays in the set and future
            // broadcasts to it silently fail or log warnings.
            eventBus.unsubscribeAll(this);
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    // Message dispatcher

    private void dispatch(Message msg) {
        try {
            switch (msg.getType()) {
                case REGISTER            -> handleRegister(msg);
                case LOGIN               -> handleLogin(msg);
                case LOGOUT              -> handleLogout(msg);
                case GET_AUCTIONS        -> handleGetAuctions(msg);
                case GET_AUCTION_DETAIL  -> handleGetAuctionDetail(msg);
                case GET_BID_HISTORY     -> handleGetBidHistory(msg);
                case PLACE_BID           -> handlePlaceBid(msg);
                case SET_AUTO_BID        -> handleSetAutoBid(msg);
                case CREATE_ITEM         -> handleCreateItem(msg);
                case CREATE_AUCTION      -> handleCreateAuction(msg);
                case CANCEL_AUCTION      -> handleCancelAuction(msg);
                case WATCH_AUCTION       -> handleWatchAuction(msg);
                case UNWATCH_AUCTION     -> handleUnwatchAuction(msg);
                case GET_SELLER_AUCTIONS -> handleGetSellerAuctions(msg);
                case GET_SELLER_ITEMS    -> handleGetSellerItems(msg);
                case GET_USERS           -> handleGetUsers(msg);
                case BAN_USER            -> handleBanUser(msg);
                default -> sendError(msg.getRequestId(),
                        "Unknown message type: " + msg.getType());
            }
        } catch (AuthException | BidException | AuctionException e) {
            // Expected business rule violations - send a clean error message to the client.
            sendError(msg.getRequestId(), e.getMessage());
        } catch (Exception e) {
            // Unexpected errors - log the full stack trace server-side, send generic message.
            
            sendError(msg.getRequestId(), "Internal server error");
        }
    }

    // Auth handlers

    private void handleRegister(Message msg) {
        RegisterRequest req = msg.parsePayload(gson, RegisterRequest.class);
        User user = userManager.register(req.username, req.password, req.email, req.role);
        send(Message.reply(msg.getRequestId(), MessageType.REGISTER_RESPONSE,
                DtoMapper.toDto(user), gson));
    }

    private void handleLogin(Message msg) {
        LoginRequest req = msg.parsePayload(gson, LoginRequest.class);
        currentUser = userManager.login(req.username, req.password);
        send(Message.reply(msg.getRequestId(), MessageType.LOGIN_RESPONSE,
                DtoMapper.toDto(currentUser), gson));
    }

    private void handleLogout(Message msg) {
        eventBus.unsubscribeAll(this);
        currentUser = null;
        send(Message.reply(msg.getRequestId(), MessageType.LOGOUT, "OK", gson));
    }

    // Auction query handlers

    private void handleGetAuctions(Message msg) {
        List<AuctionDTO> dtos = auctionManager.getAllAuctions().stream()
                .map(DtoMapper::toDto)      // convert each Auction -> AuctionDTO
                .collect(Collectors.toList());
        send(Message.reply(msg.getRequestId(), MessageType.AUCTIONS_RESPONSE,
                new AuctionsResponse(dtos), gson));
    }

    private void handleGetAuctionDetail(Message msg) {
        GetAuctionDetailRequest req = msg.parsePayload(gson, GetAuctionDetailRequest.class);
        AuctionDTO dto = DtoMapper.toDto(auctionManager.getAuction(req.auctionId));
        send(Message.reply(msg.getRequestId(), MessageType.AUCTION_DETAIL_RESPONSE, dto, gson));
    }

    private void handleGetBidHistory(Message msg) {
        GetBidHistoryRequest req = msg.parsePayload(gson, GetBidHistoryRequest.class);
        List<BidDTO> bids = bidManager.getBidHistory(req.auctionId).stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
        send(Message.reply(msg.getRequestId(), MessageType.BID_HISTORY_RESPONSE,
                new BidHistoryResponse(req.auctionId, bids), gson));
    }

    private void handleGetSellerAuctions(Message msg) {
        requireAuth();
        List<AuctionDTO> dtos = auctionManager.getSellerAuctions(currentUser.getId()).stream()
                .map(DtoMapper::toDto).collect(Collectors.toList());
        send(Message.reply(msg.getRequestId(), MessageType.SELLER_AUCTIONS_RESPONSE,
                new AuctionsResponse(dtos), gson));
    }

    private void handleGetSellerItems(Message msg) {
        requireAuth();
        List<ItemDTO> dtos =
                itemManager.getItemsBySeller(currentUser.getId()).stream()
                .map(DtoMapper::toDto).collect(Collectors.toList());
        send(Message.reply(msg.getRequestId(), MessageType.SELLER_ITEMS_RESPONSE,
                new ItemsResponse(dtos), gson));
    }

    // Bidding handlers

    private void handlePlaceBid(Message msg) {
        requireAuth();
        PlaceBidRequest req = msg.parsePayload(gson, PlaceBidRequest.class);
        BidTransaction bid = bidManager.placeBid(req.auctionId, req.amount, currentUser);
        Auction auction = auctionManager.getAuction(req.auctionId);
        send(Message.reply(msg.getRequestId(), MessageType.BID_RESPONSE,
                new BidResponse(DtoMapper.toDto(bid), DtoMapper.toDto(auction)), gson));
    }

    private void handleSetAutoBid(Message msg) {
        requireAuth();
        SetAutoBidRequest req = msg.parsePayload(gson, SetAutoBidRequest.class);
        AutoBid ab = bidManager.setAutoBid(req.auctionId, req.maxBid, req.increment, currentUser);
        send(Message.reply(msg.getRequestId(), MessageType.AUTO_BID_RESPONSE,
                new AutoBidResponse(ab.getAuctionId(), ab.getMaxBid(), ab.getIncrement()), gson));
    }

    // Item / Auction management

    private void handleCreateItem(Message msg) {
        requireAuth();
        CreateItemRequest req = msg.parsePayload(gson, CreateItemRequest.class);
        Item item = itemManager.createItem(req.name, req.description,
                req.category, currentUser);
        send(Message.reply(msg.getRequestId(), MessageType.ITEM_CREATED,
                DtoMapper.toDto(item), gson));
    }

    private void handleCreateAuction(Message msg) {
        requireAuth();
        CreateAuctionRequest req = msg.parsePayload(gson, CreateAuctionRequest.class);
        Item item = itemManager.getItem(req.itemId);
        Auction auction = auctionManager.createAuction(
                item,
                req.startingPrice,
                LocalDateTime.parse(req.startTime, FMT),  // String -> LocalDateTime
                LocalDateTime.parse(req.endTime,   FMT),
                currentUser);
        send(Message.reply(msg.getRequestId(), MessageType.AUCTION_CREATED,
                DtoMapper.toDto(auction), gson));
    }

    private void handleCancelAuction(Message msg) {
        requireAuth();
        CancelAuctionRequest req = msg.parsePayload(gson, CancelAuctionRequest.class);
        auctionManager.cancelAuction(req.auctionId, currentUser);
        send(Message.reply(msg.getRequestId(), MessageType.AUCTION_CANCELED, "OK", gson));
    }

    // Watch / Unwatch

    private void handleWatchAuction(Message msg) {
        WatchAuctionRequest req = msg.parsePayload(gson, WatchAuctionRequest.class);
        eventBus.subscribe(req.auctionId, this); // 'this' = this ClientHandler
        send(Message.reply(msg.getRequestId(), MessageType.WATCH_AUCTION, "OK", gson));
    }

    private void handleUnwatchAuction(Message msg) {
        WatchAuctionRequest req = msg.parsePayload(gson, WatchAuctionRequest.class);
        eventBus.unsubscribe(req.auctionId, this);
        send(Message.reply(msg.getRequestId(), MessageType.UNWATCH_AUCTION, "OK", gson));
    }

    // Admin handlers

    private void handleGetUsers(Message msg) {
        requireAuth();
        requireAdmin();
        List<UserDTO> dtos = userManager.getAllUsers().stream()
                .map(DtoMapper::toDto).collect(Collectors.toList());
        send(Message.reply(msg.getRequestId(), MessageType.USERS_RESPONSE,
                new UsersResponse(dtos), gson));
    }

    private void handleBanUser(Message msg) {
        requireAuth();
        requireAdmin();
        BanUserRequest req = msg.parsePayload(gson, BanUserRequest.class);
        userManager.banUser(req.userId, currentUser);
        send(Message.reply(msg.getRequestId(), MessageType.USER_BANNED, "OK", gson));
    }

    // AuctionObserver callbacks (called from AuctionEventBus notify-pool)

    @Override
    public void onBidPlaced(Auction auction, BidTransaction bid) {
        sendBroadcast(MessageType.BID_BROADCAST,
                new BidResponse(DtoMapper.toDto(bid), DtoMapper.toDto(auction)));
    }

    @Override
    public void onAuctionEnded(Auction auction) {
        sendBroadcast(MessageType.AUCTION_END_BROADCAST, DtoMapper.toDto(auction));
    }

    @Override
    public void onAuctionExtended(Auction auction) {
        sendBroadcast(MessageType.AUCTION_EXTENDED,
                new AuctionExtendedNotice(auction.getId(),
                        auction.getEndTime().toString()));
    }

    // Wire helpers

    private synchronized void send(Message msg) {
        if (out != null) out.println(gson.toJson(msg));
    }

    private void sendBroadcast(MessageType type, Object payload) {
        send(Message.broadcast(type, payload, gson));
    }

    private void sendError(String requestId, String message) {
        Message err = requestId != null
                ? Message.reply(requestId, MessageType.ERROR,
                        new ErrorResponse(message), gson)
                : Message.broadcast(MessageType.ERROR,
                        new ErrorResponse(message), gson);
        send(err);
    }

    private void requireAuth() {
        if (currentUser == null) throw new AuthException("Not authenticated");
    }

    private void requireAdmin() {
        if (currentUser.getRole() != UserRole.ADMIN)
            throw new AuthException("Admin access required");
    }
}
