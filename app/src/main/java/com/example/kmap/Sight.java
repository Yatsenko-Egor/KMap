package com.example.kmap;

public class Sight {
    public String name;
    public int type;
    public double latitude;
    public double longitude;
    public String info;

    public Sight() {
        this.name = "";
        this.type = 0;
        this.latitude = 0;
        this.longitude = 0;
        this.info = "";
    }

    public Sight(String name, int type, double latitude, double longitude, String info) {
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.info = info;
    }
}
