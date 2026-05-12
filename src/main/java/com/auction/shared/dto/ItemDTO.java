package com.auction.shared.dto;

public class ItemDTO {

    private long id;
    private String name; // Show trong auction list
    private String description; // show trong man hinh chi tiet cua auction
    private String category;   
    private long sellerId;     
    private String sellerName; 
    private String createdAt; 

    public ItemDTO() {} 

    // Getters

    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public long getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public String getCreatedAt() { return createdAt; }

    // Setters 
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setSellerId(long sellerId) { this.sellerId = sellerId; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

}
