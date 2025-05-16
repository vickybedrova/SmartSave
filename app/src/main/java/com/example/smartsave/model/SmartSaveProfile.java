package com.example.smartsave.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.Map; // For transactions map

@IgnoreExtraProperties
public class SmartSaveProfile {

    private double savingsPercentage;
    private String startDate;
    private double totalSaved;
    private Map<String, Transaction> transactions; // To map the nested transactions

    // No-argument constructor required for Firebase
    public SmartSaveProfile() {
    }

    // Getters
    public double getSavingsPercentage() {
        return savingsPercentage;
    }

    public String getStartDate() {
        return startDate;
    }

    public double getTotalSaved() {
        return totalSaved;
    }

    public Map<String, Transaction> getTransactions() {
        return transactions;
    }

    // Setters
    public void setSavingsPercentage(double savingsPercentage) {
        this.savingsPercentage = savingsPercentage;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setTotalSaved(double totalSaved) {
        this.totalSaved = totalSaved;
    }

    public void setTransactions(Map<String, Transaction> transactions) {
        this.transactions = transactions;
    }
}