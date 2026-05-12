package com.auction.shared.protocol;

public enum MessageType {

    // Xác thực
    LOGIN,           // client -> server: gửi username + password
    LOGIN_RESPONSE,  // server -> client: trả lại UserDTO on success
    REGISTER,        // client -> server: gửi username, password, email, role
    REGISTER_RESPONSE, // server -> client: trả lại created UserDTO
    LOGOUT,          // client -> server: xóa session; server xóa watchers

    // tìm kiếm Auction (read only)
    GET_AUCTIONS,        // client -> server: đưa ra toàn bộ auctions (không cần payload, đây chỉ là tín hiệu)
    AUCTIONS_RESPONSE,   // server -> client: danh sách các AuctionDTOs
    GET_AUCTION_DETAIL,  // client -> server: đưa ra one auction theo id
    AUCTION_DETAIL_RESPONSE, // server -> client: một AuctionDTO chi tiết
    GET_BID_HISTORY,     // client -> server: đưa ra toàn bộ bids cho một auction
    BID_HISTORY_RESPONSE,    // server -> client: danh sách các BidDTOs

    // Bidding 
    PLACE_BID,      // client -> server: bid thủ công và amount
    BID_RESPONSE,   // server -> client: xác nhận bid và cập nhật
    SET_AUTO_BID,   // client -> server: đăng kí maxBid và increment cho autobid
    AUTO_BID_RESPONSE, // server -> client: xác nhận đăng kí autobid

    // Quản lý item và auction (chỉ seller và admin - nếu có)
    CREATE_ITEM,    
    ITEM_CREATED,   
    CREATE_AUCTION, 
    AUCTION_CREATED,
    CANCEL_AUCTION, 
    AUCTION_CANCELED,
    GET_SELLER_AUCTIONS, // Xem danh sách các autions của mình
    SELLER_AUCTIONS_RESPONSE, 
    GET_SELLER_ITEMS,       
    SELLER_ITEMS_RESPONSE,  

    // Admin - nếu có
    GET_USERS,   
    USERS_RESPONSE,
    BAN_USER,    
    USER_BANNED, 

    WATCH_AUCTION,   // client -> server: nhận thông báo cho aution này
    UNWATCH_AUCTION, 

    BID_BROADCAST,         
    AUCTION_END_BROADCAST, 
    AUCTION_EXTENDED,      // Antisniping

    // Error
    ERROR  
}
