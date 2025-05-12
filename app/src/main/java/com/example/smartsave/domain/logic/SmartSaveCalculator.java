package com.example.smartsave.domain.logic;
import com.example.smartsave.model.Transaction;

import java.util.List;

public class SmartSaveCalculator {
    private final double savingPercentage;
    private final double interestRate;

    public SmartSaveCalculator(double savingPercentage, double interestRate) {
        this.savingPercentage = savingPercentage;   // e.g. 3%
        this.interestRate = interestRate;           // e.g. 0.5%
    }

    public double calculateTotalSaved(List<Transaction> transactions) {
        double totalSaved = 0.0;
        for (Transaction t : transactions) {
            if (t.getAmount() > 0) {
                totalSaved += t.getAmount() * (savingPercentage / 100.0);
            }
        }
        return totalSaved;
    }

    public double calculateExpectedReturn(double totalSaved) {
        return totalSaved * (interestRate / 100.0);  // Simple monthly interest
    }

}
