package com.example.mobilnaaplikacija.model;

public class Attack {
    private int attemptNumber;
    private boolean hit;
    private double damageDealt;

    public Attack(int attemptNumber, boolean hit, double damageDealt) {
        this.attemptNumber = attemptNumber;
        this.hit = hit;
        this.damageDealt = damageDealt;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public double getDamageDealt() {
        return damageDealt;
    }

    public void setDamageDealt(double damageDealt) {
        this.damageDealt = damageDealt;
    }
}
