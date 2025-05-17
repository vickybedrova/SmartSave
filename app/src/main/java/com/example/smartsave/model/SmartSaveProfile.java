package com.example.smartsave.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.Map;

@IgnoreExtraProperties
public class SmartSaveProfile {

    private double savingsPercentage;
    private String startDate;
    private double totalSaved;
    private Map<String, Transaction> transactions;
    private boolean isActive;

    public SmartSaveProfile() {
        this.isActive = true;
    }

    public SmartSaveProfile(double savingsPercentage, String startDate, double totalSaved, boolean isActive) {
        this.savingsPercentage = savingsPercentage;
        this.startDate = startDate;
        this.totalSaved = totalSaved;
        this.isActive = isActive; // <<< SET IT HERE
    }
    public SmartSaveProfile(double savingsPercentage, String startDate, double totalSaved) {
        this(savingsPercentage, startDate, totalSaved, true);
    }


    public double getSavingsPercentage() { return savingsPercentage; }
    public String getStartDate() { return startDate; }
    public double getTotalSaved() { return totalSaved; }
    public Map<String, Transaction> getTransactions() { return transactions; }
    public boolean isActive() { return isActive; }

    public void setSavingsPercentage(double savingsPercentage) { this.savingsPercentage = savingsPercentage; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setTotalSaved(double totalSaved) { this.totalSaved = totalSaved; }
    public void setTransactions(Map<String, Transaction> transactions) { this.transactions = transactions; }
    public void setActive(boolean active) { isActive = active; }
}