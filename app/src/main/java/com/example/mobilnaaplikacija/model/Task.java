package com.example.mobilnaaplikacija.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;

public class Task implements Parcelable {
    public Long Id;
    public String Name;
    public String Description;
    public String Category;
    public FrequencyType Frequency;
    public String StartDate;
    public String EndDate;
    public String Time;
    public Boolean IsWholeDay;
    public Integer Interval;
    public UnitType Unit;
    public DifficultyType Difficulty;
    public ImportanceType Importance;
    public StatusType Status;

    public Task() {}

    public Task(String name, String description, String category, FrequencyType frequency, String startDate, String endDate, String time, Boolean isWholeDay, Integer interval, UnitType unit, DifficultyType difficulty, ImportanceType importance, StatusType status) {
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

    public Task(Long id, String name, String description, String category, FrequencyType frequency, String startDate, String endDate, String time, Boolean isWholeDay, Integer interval, UnitType unit, DifficultyType difficulty, ImportanceType importance, StatusType status) {
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
        String freqName = in.readString();
        Frequency = freqName != null ? FrequencyType.valueOf(freqName) : null;
        StartDate = in.readString();
        EndDate = in.readString();
        Time = in.readString();
        IsWholeDay = in.readBoolean();
        Interval = in.readInt();
        String unitName = in.readString();
        Unit = unitName != null ? UnitType.valueOf(unitName) : null;
        String diffName = in.readString();
        Difficulty = diffName != null ? DifficultyType.valueOf(diffName) : null;
        String impName = in.readString();
        Importance = impName != null ? ImportanceType.valueOf(impName) : null;
        String statusName = in.readString();
        Status = statusName != null ? StatusType.valueOf(statusName) : null;
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

    public FrequencyType getFrequency() {
        return Frequency;
    }

    public void setFrequency(FrequencyType frequency) {
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

    public UnitType getUnit() {
        return Unit;
    }

    public void setUnit(UnitType unit) {
        Unit = unit;
    }

    public DifficultyType getDifficulty() {
        return Difficulty;
    }

    public void setDifficulty(DifficultyType difficulty) {
        Difficulty = difficulty;
    }

    public ImportanceType getImportance() {
        return Importance;
    }

    public void setImportance(ImportanceType importance) {
        Importance = importance;
    }

    public StatusType getStatus() {
        return Status;
    }

    public void setStatus(StatusType status) {
        Status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeLong(Id != null ? Id : -1);
        parcel.writeString(Name);
        parcel.writeString(Description);
        parcel.writeString(Category);
        parcel.writeString(Frequency != null ? Frequency.name() : null);
        parcel.writeString(StartDate);
        parcel.writeString(EndDate);
        parcel.writeString(Time);
        parcel.writeBoolean(IsWholeDay);
        parcel.writeInt(Interval != null ? Interval : 0);
        parcel.writeString(Unit != null ? Unit.name() : null);
        parcel.writeString(Difficulty != null ? Difficulty.name() : null);
        parcel.writeString(Importance != null ? Importance.name() : null);
        parcel.writeString(Status != null ? Status.name() : null);
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
