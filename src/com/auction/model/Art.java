package com.auction.model;
public class Art extends Item {
    private String artist;
    private String ArtGenre;
    public Art(String name, String description, double startingPrice, String artist, String ArtGenre) {
        super(name, description, startingPrice);
        artist = this.artist;
        ArtGenre = this.ArtGenre;
    }
    public String getartist() {
        return artist;
    }
    public String getArtGenre() {
        return ArtGenre;
    }
    @Override
    public String getDetails() {
        return "[Art] " + getName() + " | Artist: " + artist + " ArtGenre: " + ArtGenre + " | Description: " + getDescription();
    }
}
