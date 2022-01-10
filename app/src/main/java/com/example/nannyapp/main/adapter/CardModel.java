package com.example.nannyapp.main.adapter;

public class CardModel {
    private String fullName;
    private String location;
    private String rating;
    private byte[] profilePicture;

    public CardModel() { }

    public CardModel(String fullName, String location, String rating, byte[] profilePicture) {
        this.fullName = fullName;
        this.location = location;
        this.rating = rating;
        this.profilePicture = profilePicture;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }
}
