package com.auction.shared.models;

public class Art extends Item {
    private String artist;

    public Art(String name, double startingPrice) {
        super(name, startingPrice);
    }
    public Art(String name, double startingPrice, String artist) {
        super(name, startingPrice);
        this.artist = artist;
    }
    //getter
    public String getArtist() {return artist;}
    //setter
    public void setArtist(String artist) {this.artist = artist;}
    @Override
    public String getInfo() {
        return "[Art] " + getName() + " | Description: " + getDescription();
    }
}
