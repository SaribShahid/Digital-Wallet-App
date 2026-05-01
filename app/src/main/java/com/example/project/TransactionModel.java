package com.example.project;

public class TransactionModel {
    public String type;
    public double amount;
    public String otherParty;
    public long timestamp;

    public TransactionModel() {}

    public TransactionModel(String type, double amount, String otherParty, long timestamp) {
        this.type = type;
        this.amount = amount;
        this.otherParty = otherParty;
        this.timestamp = timestamp;
    }
}


