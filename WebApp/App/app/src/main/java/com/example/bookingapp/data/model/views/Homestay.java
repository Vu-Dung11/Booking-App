package com.example.bookingapp.data.model.views;

public class Homestay {
    private String imageUrl;
    private String name;
    private String location;
    private Double price;
    private Double rating;
    private boolean isFavorite;

    public Homestay(String imageUrl, String name, String location, Double price, Double rating, boolean isFavorite) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.location = location;
        this.price = price;
        this.rating = rating;
        this.isFavorite = isFavorite;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
