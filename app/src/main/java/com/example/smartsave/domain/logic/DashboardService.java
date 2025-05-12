package com.example.smartsave.domain.logic;
import com.example.smartsave.data.MyPosTransactionService;
import com.example.smartsave.model.DashboardState;
import com.example.smartsave.model.Transaction;

import java.util.List;
public class DashboardService {
    private final MyPosTransactionService transactionService;
    private final SmartSaveCalculator calculator;

    public DashboardService(MyPosTransactionService transactionService, SmartSaveCalculator calculator) {
        this.transactionService = transactionService;
        this.calculator = calculator;
    }

    public DashboardState getDashboardDataForCurrentMonth() {
        List<Transaction> transactions = transactionService.getTransactionsForCurrentMonth();
        double totalSaved = calculator.calculateTotalSaved(transactions);
        double expectedReturn = calculator.calculateExpectedReturn(totalSaved);

        return new DashboardState(totalSaved, expectedReturn, transactions);
    }
}
