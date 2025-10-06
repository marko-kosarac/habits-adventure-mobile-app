package com.example.mobilnaaplikacija.model;

public class Boss {
    private String id;
    private double currentHp;
    private double maxHp;
    private int level; //currently fighting boss (1st, 2nd...)
    private boolean defeated;

    public Boss (String id, int maxHp, boolean defeated) {
        this.id = id;
        this.currentHp = maxHp;
        this.maxHp = maxHp;
        this.level = 1;
        this.defeated = defeated;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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
    public boolean getDefeated() { return defeated; }

    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }


}
