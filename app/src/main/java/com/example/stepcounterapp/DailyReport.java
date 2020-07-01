package com.example.stepcounterapp;


//this class is the model class for the Daily reports that is going to be used to present the values in the converter fragment and report fragment
public class DailyReport {
    private int totalSteps;
    private int calories;
    private double distance;
    private String date;


    public DailyReport(int totalSteps, int calories, String date,double distance) {
        this.totalSteps = totalSteps;
        this.calories = calories;
        this.date = date;
        this.distance=distance;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public int getCalories() {
        return calories;
    }

    public String getDate() {
        return date;
    }

    public double getDistance() {
        return distance;
    }
}
