package com.example.smartsave.model;

public class SimpleTransaction {
    private String title;
    private String date;
    private double amount;
    private double savings;


    public SimpleTransaction(String title, String date, double amount, double savings) {
        this.title = title;
        this.date = date;
        this.amount = amount;
        this.savings = savings;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getSavings() {
        return savings;
    }

    public void setSavings(double savings) {
        this.savings = savings;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
};
