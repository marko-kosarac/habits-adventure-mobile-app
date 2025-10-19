package com.example.mobilnaaplikacija.model;

import java.util.List;

public class SpecialMission {
    private String id;
    private String leaderId;
    private boolean isStarted;
    private boolean isDone;
    private List<String> members;
    private long startTime;
    private long endTime;

    public SpecialMission() {}

    public SpecialMission(String id, String leaderId, boolean isStarted, boolean isDone, List<String> members, long startTime, long endTime) {
        this.id = id;
        this.leaderId = leaderId;
        this.isStarted = isStarted;
        this.isDone = isDone;
        this.members = members;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getteri i setteri

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLeaderId() { return leaderId; }
    public void setLeaderId(String leaderId) { this.leaderId = leaderId; }

    public boolean isStarted() { return isStarted; }
    public void setStarted(boolean started) { isStarted = started; }

    public boolean isDone() { return isDone; }
    public void setDone(boolean done) { isDone = done; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
}
