package com.gongzetao.loop.bean;

import java.io.Serializable;

/**
 * Created by baixinping on 2016/9/19.
 */
public class Position implements Serializable {
    public static final String currentPosition = "currentPosition";

    private double lon;
    private double lat;
    private String name;

    public Position(double lon, double lat, String name) {
        this.lon = lon;
        this.lat = lat;
        this.name = name;
    }
    public Position() {

    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
