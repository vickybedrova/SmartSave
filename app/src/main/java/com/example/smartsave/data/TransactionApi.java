package com.example.smartsave.data;

import com.example.smartsave.model.Transaction;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface TransactionApi {
    @GET("transactions/get-all")
    Call<List<Transaction>> getAllTransactions();
}
