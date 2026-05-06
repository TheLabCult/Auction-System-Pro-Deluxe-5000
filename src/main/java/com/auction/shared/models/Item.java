package com.auction.shared.models;

import com.auction.shared.enums.*;

public abstract class Item extends Entity {

    private String name;
    private String description;
    private long sellerId;
    private String sellerName;
    
    protected Item() { super(); }
    

    /*Dùng bởi SQLiteItemDAO và SQLiteAuctionDAO khi quyết định xem cần
    tái tạo đối tượng loại nào khi lấy ra khỏi db, hoặc khi đưa vào db */
    public abstract ItemCategory getCategory();

    @Override
    public void printInfo() {
        System.out.printf("[%s] id=%d  name=%-30s  seller=%s%n",
                getCategory(), id, name, sellerName);
    }

    /*Getters/Setters ====================================== */

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getSellerId() { return sellerId; }
    public void setSellerId(long sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
}
