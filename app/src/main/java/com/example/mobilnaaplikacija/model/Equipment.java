package com.example.mobilnaaplikacija.model;

public class Equipment {
    private long id;
    private String name;
    private String description;
    private String type;    // Napitak / Odeća / Oružje
    private String bonus;   // "+20% PP", "+10% Snage", itd.
    private int duration;   // trajanje u borbama (0 = jednokratni, >0 = trajno)

    public Equipment() {
    }

    public Equipment(long id, String name, String description, String type, String bonus, int duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.bonus = bonus;
        this.duration = duration;
    }

    // Getteri i setteri
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getBonus() { return bonus; }
    public void setBonus(String bonus) { this.bonus = bonus; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
}
