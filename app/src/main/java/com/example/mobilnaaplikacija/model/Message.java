package com.example.mobilnaaplikacija.model;

public class Message {
    public String fromUserId;
    public String username;
    public String message;
    public long timestamp;

    public Message() {}

    public Message(String fromUserId, String username, String message, long timestamp) {
        this.fromUserId = fromUserId;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
    }
}
