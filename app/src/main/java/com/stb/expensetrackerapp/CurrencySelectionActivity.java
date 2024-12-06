package com.stb.expensetrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CurrencySelectionActivity extends AppCompatActivity {

    private ListView listViewCurrencies;
    private Button btnSaveCurrency;
    private ArrayList<String> currencyList;
    private FirebaseFirestore db;
    private String selectedCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_selection);

        db = FirebaseFirestore.getInstance();

        listViewCurrencies = findViewById(R.id.listViewCurrencies);
        btnSaveCurrency = findViewById(R.id.btnSaveCurrency);

        currencyList = new ArrayList<>();
        currencyList.add("USD - United States Dollar");
        currencyList.add("EUR - Euro");
        currencyList.add("INR - Indian Rupee");
        currencyList.add("GBP - British Pound");
        currencyList.add("PKR - Pakistani Rupee");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, currencyList);
        listViewCurrencies.setAdapter(adapter);

        listViewCurrencies.setOnItemClickListener((parent, view, position, id) -> {
            selectedCurrency = currencyList.get(position);
        });

        btnSaveCurrency.setOnClickListener(v -> {
            if (selectedCurrency != null) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                db.collection("users").document(userId)
                        .update("currency", selectedCurrency)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(CurrencySelectionActivity.this, "Currency saved successfully", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(CurrencySelectionActivity.this, FinancialDetailsActivity.class);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(CurrencySelectionActivity.this, "Error saving currency: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(CurrencySelectionActivity.this, "Please select a currency", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
