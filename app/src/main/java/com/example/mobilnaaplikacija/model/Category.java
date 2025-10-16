package com.example.mobilnaaplikacija.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Category implements Parcelable {
    private String id;
    private String userId;
    private String name;
    private Integer color;

    public Category() {
    }

    public Category(String id, String userId, Integer color, String name) {
        this.id = id;
        this.userId = userId;
        this.color = color;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) { this.name = name; }

    public Integer getColor() { return color; }

    public void setColor(Integer color) {
        this.color = color;
    }

    protected Category(Parcel in) {
        id = in.readString();
        name = in.readString();
        color = in.readInt();
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id != null ? id : "-1");
        parcel.writeString(name);
        parcel.writeInt(color != null ? color : Color.WHITE);
    }

    public static final Creator<Category> CREATOR = new Creator<>() {
        @Override
        public Category createFromParcel(Parcel in) { return new Category(in); }
        @Override
        public Category[] newArray(int size) { return new Category[size]; }
    };
}
