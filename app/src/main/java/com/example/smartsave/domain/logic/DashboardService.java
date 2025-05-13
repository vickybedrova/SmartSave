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

    public interface DashboardCallback {
        void onSuccess(DashboardState state);
        void onFailure(Throwable t);
    }

    public void getDashboardDataForCurrentMonth(DashboardCallback callback) {
        transactionService.getTransactionsForCurrentMonth(new MyPosTransactionService.TransactionCallback() {
            @Override
            public void onSuccess(List<Transaction> transactions) {
                double totalSaved = calculator.calculateTotalSaved(transactions);
                double expectedReturn = calculator.calculateExpectedReturn(totalSaved);
                DashboardState state = new DashboardState(totalSaved, expectedReturn, transactions);
                callback.onSuccess(state);
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }
}
