package com.example.android.autoclick.model;

public class User {
    private String date;
    private String location;
    private boolean isLimit;
    private String android;
    private String access;

    public String getAndroid() {
        return android;
    }

    public User(){}

    public void setAndroid(String android) {
        this.android = android;
    }

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

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }
}
