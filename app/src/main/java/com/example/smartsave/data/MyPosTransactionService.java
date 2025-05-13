package com.example.smartsave.data;

import android.app.Activity;
import android.util.Log;
import com.example.smartsave.model.Transaction;
import com.mypos.smartsdk.Currency;
import com.mypos.smartsdk.MyPOSAPI;
import com.mypos.smartsdk.MyPOSPayment;
import com.mypos.smartsdk.MyPOSUtil;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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


    public interface TransactionCallback {
        void onSuccess(List<Transaction> transactions);
        void onFailure(Throwable t);
    }

    public void getTransactionsForCurrentMonth(TransactionCallback callback) {
        TransactionApi api = RetrofitClient.getTransactionApi();
        Call<List<Transaction>> call = api.getAllTransactions();

        call.enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure(new Exception("Failed to get transactions"));
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }


}
