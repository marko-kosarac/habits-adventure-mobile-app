package com.example.mobilnaaplikacija.model;

public class Member {
    private String name;
    private int avatarResId;

    public Member(String name, int avatarResId) {
        this.name = name;
        this.avatarResId = avatarResId;
    }

    public String getName() { return name; }
    public int getAvatarResId() { return avatarResId; }
}

