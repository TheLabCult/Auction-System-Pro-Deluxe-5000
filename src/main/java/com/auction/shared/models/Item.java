package com.auction.shared.models;
import com.auction.shared.enums.*;
public abstract class Item extends Entity {
    private String name;
    private String description;
    private double startingPrice;
    private ItemStatus status;
    public Item(String name, double startingPrice) {
        super();
        this.name = name;
        this.description = "No description provided.";
        this.startingPrice = startingPrice;
        //this.status = ItemStatus.AVAILABLE;
    }
    public Item(String name, String description, double startingPrice) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.status = ItemStatus.AVAILABLE;
    }
    //setter
    public void setStatus(ItemStatus status) { this.status = status; }
    public void setDescription(String description) {
        this.description = description;
    }
    //getter
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public double getStartingPrice() {
        return startingPrice;
    }
    public ItemStatus getStatus() { return status; }
    public abstract String getInfo();
    
    @Override
    public String toString() {
        return name + " (Starting Price: " + startingPrice + ")";
    }
    
    public static Item createItem(ItemType type, String name, double startingPrice, String...extra) {
        switch(type) {
            case ELECTRONICS:
                //extra[0] = brand, extra[1] = warrantyMonths
                String electronicBrand = extra.length > 0 ? extra[0] : "Unknown Brand";
                int warranty = extra.length > 1 ? Integer.parseInt(extra[1]) : 0;
                return new Electronics(name, startingPrice, electronicBrand, warranty);
            case ART:
                //extra[0] = artist, extra[1] = genre;
                String artist = extra.length > 1 ? extra[0] : "Unknown Artist";
                return new Art(name, startingPrice, artist);
            case VEHICLE:
                //extra[0] = brand, extra[1] = year
                String vehicleBrand = extra.length > 1 ? extra[0] : "Unknown Brand";
                return new Vehicle(name, startingPrice, vehicleBrand);
            default:
                throw new IllegalArgumentException("Invalid Item");
        }
    }
}
