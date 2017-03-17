package com.gongzetao.loop.bean;

/**
 * Created by baixinping on 2016/10/5.
 */
public class Label {
    String name;
    String details;
    int color;

    public Label(String name, String details, int color) {
        this.name = name;
        this.color = color;
        this.details = details;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }
    public String getDetails() {
        return details;
    }


}
