package com.example.project;

public class MessageModel {

    private String title;
    private String body;
    private long timestamp;

    // Required empty constructor for Firestore
    public MessageModel() {}

    public MessageModel(String title, String body, long timestamp) {
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
