package com.example.android.autoclick.model;

public class User {
    private String date;
    private String location;
    private boolean isLimit;

    public User(){}

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isLimit() {
        return isLimit;
    }

    public void setLimit(boolean limit) {
        isLimit = limit;
    }
}
