package com.example.mobilnaaplikacija.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Task implements Parcelable {
    public Long Id;
    public String Name;
    public String Description;
    public String Category;
    public String Frequency;
    public String StartDate;
    public String EndDate;
    public String Time;
    public Boolean IsWholeDay;
    public Integer Interval;
    public String Unit;
    public String Difficulty;
    public String Importance;
    public String Status;

    public Task() {}

    public Task(String name, String description, String category, String frequency, String startDate, String endDate, String time, Boolean isWholeDay, Integer interval, String unit, String difficulty, String importance, String status) {
        Name = name;
        Description = description;
        Category = category;
        Frequency = frequency;
        StartDate = startDate;
        EndDate = endDate;
        Time = time;
        IsWholeDay = isWholeDay;
        Interval = interval;
        Unit = unit;
        Difficulty = difficulty;
        Importance = importance;
        Status = status;
    }

    public Task(Long id, String name, String description, String category, String frequency, String startDate, String endDate, String time, Boolean isWholeDay, Integer interval, String unit, String difficulty, String importance, String status) {
        Id = id;
        Name = name;
        Description = description;
        Category = category;
        Frequency = frequency;
        StartDate = startDate;
        EndDate = endDate;
        Time = time;
        IsWholeDay = isWholeDay;
        Interval = interval;
        Unit = unit;
        Difficulty = difficulty;
        Importance = importance;
        Status = status;
    }

    protected Task(Parcel in) {
        Id = in.readLong();
        Name = in.readString();
        Description = in.readString();
        Category = in.readString();
        Frequency = in.readString();
        StartDate = in.readString();
        EndDate = in.readString();
        Time = in.readString();
        IsWholeDay = in.readBoolean();
        Interval = in.readInt();
        Unit = in.readString();
        Difficulty = in.readString();
        Importance = in.readString();
        Status = in.readString();
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getFrequency() {
        return Frequency;
    }

    public void setFrequency(String frequency) {
        Frequency = frequency;
    }

    public String getStartDate() {
        return StartDate;
    }

    public void setStartDate(String startDate) {
        StartDate = startDate;
    }

    public String getEndDate() {
        return EndDate;
    }

    public void setEndDate(String endDate) {
        EndDate = endDate;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public Boolean getWholeDay() {
        return IsWholeDay;
    }

    public void setWholeDay(Boolean wholeDay) {
        IsWholeDay = wholeDay;
    }

    public Integer getInterval() {
        return Interval;
    }

    public void setInterval(Integer interval) {
        Interval = interval;
    }

    public String getUnit() {
        return Unit;
    }

    public void setUnit(String unit) {
        Unit = unit;
    }

    public String getDifficulty() {
        return Difficulty;
    }

    public void setDifficulty(String difficulty) {
        Difficulty = difficulty;
    }

    public String getImportance() {
        return Importance;
    }

    public void setImportance(String importance) {
        Importance = importance;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeLong(Id);
        parcel.writeString(Name);
        parcel.writeString(Description);
        parcel.writeString(Category);
        parcel.writeString(Frequency);
        parcel.writeString(StartDate);
        parcel.writeString(EndDate);
        parcel.writeString(Time);
        parcel.writeBoolean(IsWholeDay);
        parcel.writeInt(Interval);
        parcel.writeString(Unit);
        parcel.writeString(Difficulty);
        parcel.writeString(Importance);
        parcel.writeString(Status);
    }


    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
}
