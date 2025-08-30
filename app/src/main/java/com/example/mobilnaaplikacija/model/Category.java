package com.example.mobilnaaplikacija.model;

public class Category {
    private String Name;
    private Integer Color;

    public Category(Integer color, String name) {
        Color = color;
        Name = name;
    }

    public String getName() {
        return Name;
    }

    public Integer getColor() {
        return Color;
    }

    public void setColor(Integer color) {
        Color = color;
    }
}
