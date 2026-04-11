package com.auction.shared.models;

public class Electronics extends Item {
    private String brand;
    private int warrantyMonths;
    public Electronics(String name, double startingPrice)  {
        super(name, startingPrice);
    }
    public Electronics(String name, double startingPrice, String brand, int warrantyMonths) {
        super(name, startingPrice);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }
    //getter
    public String getBrand() {
        return brand;
    }
    public int getWarrantyMonths() {
        return warrantyMonths;
    }
    //setter
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }
    //method
    @Override
    public String getInfo() {
        return "[Electronics] " + getName() + " | Description: " + getDescription();
    }
}
