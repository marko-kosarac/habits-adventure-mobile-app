package com.example.mobilnaaplikacija.model;

public class Boss {
    private String id;
    private double maxHp;
    private double currentHp;
    private boolean defeated;

    public Boss (String id) {
        this.id = id;
        this.maxHp = 200;
        this.currentHp = 200;
        this.defeated = false;
    }

    public void takeDamage(double damage) { //TODO into service
        currentHp -= damage;
        if (currentHp < 0) currentHp = 0;
        if (currentHp == 0) defeated = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(double maxHp) {
        this.maxHp = maxHp;
    }

    public double getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(double currentHp) {
        this.currentHp = currentHp;
    }

    public boolean isDefeated() {
        return defeated;
    }

    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }
}
