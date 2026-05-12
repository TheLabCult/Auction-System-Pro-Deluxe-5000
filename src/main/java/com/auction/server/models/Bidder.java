package com.auction.server.models;

import com.auction.server.enums.UserRole;

/*
Bidder có thể:
- Xem toàn bộ auctions
- Đặt bid thủ công
- Cài auto-bid

Bidder không thể:
- Mở auction hay tạo item
- Không có quyền admin

UserFactory tạo một Bidder khi role đăng kí là BIDDER
SQLiteUserDao tái tạo một Bidder khi hàng role là BIDDER
*/

public class Bidder extends User{
    public Bidder() { super(); }
    
    @Override public UserRole getRole() { return UserRole.BIDDER; }

    @Override public boolean canBid() { return true; }
    @Override public boolean canSell() { return false; }
}

