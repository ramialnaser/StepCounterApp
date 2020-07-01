package com.example.stepcounterapp;

// this is the user model class that is used in the profile activity to send an object of the user to the API
public class User {
    private String userId;
    private String gender;
    private int height;
    private int weight;

    public User(String userId, String gender, int height, int weight) {
        this.userId = userId;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
    }

    public User() {
    }


    public String getUserId() {
        return userId;
    }

    public String getGender() {
        return gender;
    }

    public int getHeight() {
        return height;
    }

    public int getWeight() {
        return weight;
    }
}
