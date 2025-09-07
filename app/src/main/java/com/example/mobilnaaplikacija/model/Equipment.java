package com.example.mobilnaaplikacija.model;

public class Equipment {

    public enum Type {
        NAPITAK,
        ODECA,
        ORUZJE
    }

    private long id;
    private String name;
    private String description;
    private Type type;      // enum umesto stringa
    private String bonus;   // "+20% PP", "+10% Snage", itd.
    private int duration;   // trajanje u borbama (0 = jednokratni, >0 = trajno)
    private int quantity;
    private int price;
    private boolean isActivated;

    public Equipment() {}

    public Equipment(long id, String name, String description, Type type, String bonus, int duration, int price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.bonus = bonus;
        this.duration = duration;
        this.price = price;
    }

    // Getteri i setteri
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getBonus() { return bonus; }
    public void setBonus(String bonus) { this.bonus = bonus; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public int getPrice(){return price;}
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(int price){this.price = price;}

    public boolean isActive() {
        return isActivated;
    }

    public void setActive(boolean active) {
        isActivated = active;
    }
}
