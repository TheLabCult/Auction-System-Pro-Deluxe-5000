package main.java.com.auction.shared.models.item;

import main.java.com.auction.shared.models.Entity;
import main.java.com.auction.shared.enums.ItemType;

abstract public class Item extends Entity {
    //constructors
    private double price;
    private int stock;
    public Item(double price, int stock) {
        this.price = price;
        this.stock = stock;
    }

    //factory method
    public static Item createItem(ItemType type, double price, int stock) {
        switch (type) {
            case ELECTRONICS:
                return new Electronics(price, stock);
            case ARTS:
                return new Arts(price, stock);
            case VEHICLES:
                return new Vehicles(price, stock);
            default:
                throw new IllegalArgumentException("Không tồn tại loai " + type + ". Vui lòng thử lại sau.");
        }
    }
}