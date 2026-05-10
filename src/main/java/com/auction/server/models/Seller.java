package com.auction.server.models;

import com.auction.server.enums.UserRole;

/*
Seller có thể:
- Tạo items (CREATE_ITEM request)
- Mở auction cho items của họ (CREATE_AUCTION request)
- Hủy OPEN/RUNNING auction của họ (CANCEL_AUCTION request)
- Xem tất cả auctions (tất cả user đều có thể)

Seller không thể:
- Đặt bid 
- Có chức năng của admin (ban)
*/
public class Seller extends User {
    public Seller() { super(); }

    @Override public UserRole getRole() { return UserRole.SELLER; }

    @Override public boolean canBid() { return false; }
    @Override public boolean canSell() { return true; }

}
