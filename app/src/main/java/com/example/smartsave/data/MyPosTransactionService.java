package com.example.smartsave.data;

import android.app.Activity;
import android.content.Intent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import android.util.Log;

import com.example.smartsave.model.Transaction;
import com.mypos.smartsdk.Currency;
import com.mypos.smartsdk.MyPOSAPI;
import com.mypos.smartsdk.MyPOSPayment;
import com.mypos.smartsdk.MyPOSUtil;
import java.util.Locale;



import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;




public class MyPosTransactionService {
    private static final int PAYMENT_REQUEST_CODE = 1001;
    //to initiate payment - uses myPOS Smart SDK
    public void startPayment(Activity activity, double amount) {
        try {
            MyPOSPayment payment = MyPOSPayment.builder()
                    .productAmount(amount)
                    .currency(Currency.EUR)
                    .foreignTransactionId(UUID.randomUUID().toString())
                    .printMerchantReceipt(MyPOSUtil.RECEIPT_ON)
                    .printCustomerReceipt(MyPOSUtil.RECEIPT_ON)
                    .build();
            MyPOSAPI.openPaymentActivity(activity, payment, PAYMENT_REQUEST_CODE);

        }catch (Exception e) {
            Log.e("SmartSave", "Payment initiation failed", e);
        }
    }

    public List<Transaction> getTransactionsForCurrentMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d", Locale.ENGLISH);

        try {
            return Arrays.asList(
                    new Transaction("tx001", "Food Shopping", sdf.parse("July 16"), -400.0),
                    new Transaction("tx002", "Salary Payment", sdf.parse("July 15"), 8000.0),
                    new Transaction("tx003", "Health Expenses", sdf.parse("July 14"), -370.0),
                    new Transaction("tx004", "Freelance Payment", sdf.parse("July 10"), 1200.0),
                    new Transaction("tx005", "House Bills", sdf.parse("July 9"), -3100.0)
            );
        } catch (ParseException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


}
