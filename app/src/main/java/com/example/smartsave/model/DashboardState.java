package com.example.smartsave.model;
import java.util.List;

public class DashboardState {
    private double totalSaved;
    private double expectedReturn;
    private List<Transaction> recentTransactions;

    public DashboardState(double totalSaved, double expectedReturn, List<Transaction> recentTransactions) {
        this.totalSaved = totalSaved;
        this.expectedReturn = expectedReturn;
        this.recentTransactions = recentTransactions;
    }

    public double getTotalSaved() { return totalSaved; }
    public double getExpectedReturn() { return expectedReturn; }
    public List<Transaction> getRecentTransactions() { return recentTransactions; }

}
