package com.example.mobilnaaplikacija.model;

public class Category {
    private String id;
    private String name;
    private Integer color;

    public Category() {
    }

    public Category(String id, Integer color, String name) {
        id = id;
        color = color;
        name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) { this.name = name; }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }
}
