package com.example.smartsave.model;

import java.util.Date;

public class Transaction {
    private String id;
    private String description;
    private Date date;
    private double amount;

    public Transaction(String id, String description, Date date, double amount) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.amount = amount;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public Date getDate() { return date; }
    public double getAmount() { return amount; }
}
