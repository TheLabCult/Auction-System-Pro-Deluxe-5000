package com.auction.shared.models;

public class Vehicle extends Item{
    private String brand;
    public Vehicle(String name, double startingPrice) {
        super(name, startingPrice);
    }
    public Vehicle(String name, double startingPrice, String brand) {
        super(name, startingPrice);
        this.brand = brand;
    }
    //getter
    public String getBrand() {
        return brand;
    }
    //setter
    public void setBrand(String brand) {
        this.brand = brand;
    }
    @Override
    public String getInfo() {
        return "[Vehicle] " + getName() + " | Description: " + getDescription();
    }
}
