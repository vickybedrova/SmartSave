package com.example.smartsave.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.Map;

@IgnoreExtraProperties
public class SmartSaveProfile {

    private double savingsPercentage;
    private String startDate;
    private double totalSaved;
    private Map<String, Transaction> transactions; // For reading transactions if needed
    private boolean isActive; // <<< NEW FIELD

    // No-argument constructor REQUIRED for Firebase
    public SmartSaveProfile() {
        this.isActive = true; // Default to active when object is created by Firebase if field missing
    }

    // Constructor for programmatic creation (e.g., new profile setup)
    public SmartSaveProfile(double savingsPercentage, String startDate, double totalSaved, boolean isActive) {
        this.savingsPercentage = savingsPercentage;
        this.startDate = startDate;
        this.totalSaved = totalSaved;
        this.isActive = isActive; // <<< SET IT HERE
    }
    // Overload for when you were creating without isActive initially
    public SmartSaveProfile(double savingsPercentage, String startDate, double totalSaved) {
        this(savingsPercentage, startDate, totalSaved, true); // Default to active
    }


    // --- Getters ---
    public double getSavingsPercentage() { return savingsPercentage; }
    public String getStartDate() { return startDate; }
    public double getTotalSaved() { return totalSaved; }
    public Map<String, Transaction> getTransactions() { return transactions; }
    public boolean isActive() { return isActive; } // <<< NEW GETTER (use 'is' prefix for boolean)

    // --- Setters ---
    public void setSavingsPercentage(double savingsPercentage) { this.savingsPercentage = savingsPercentage; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setTotalSaved(double totalSaved) { this.totalSaved = totalSaved; }
    public void setTransactions(Map<String, Transaction> transactions) { this.transactions = transactions; }
    public void setActive(boolean active) { isActive = active; } // <<< NEW SETTER
}