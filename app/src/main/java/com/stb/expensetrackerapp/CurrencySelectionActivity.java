package com.stb.expensetrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.LinearLayout;
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
    private int selectedPosition = -1;

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

        CurrencyAdapter adapter = new CurrencyAdapter(currencyList);
        listViewCurrencies.setAdapter(adapter);

        listViewCurrencies.setOnItemClickListener((parent, view, position, id) -> {
            selectedCurrency = currencyList.get(position);
            selectedPosition = position;
            adapter.notifyDataSetChanged();
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

    private class CurrencyAdapter extends ArrayAdapter<String> {
        public CurrencyAdapter(ArrayList<String> currencies) {
            super(CurrencySelectionActivity.this, android.R.layout.simple_list_item_1, currencies);
        }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = view.findViewById(android.R.id.text1);

            textView.setTextColor(getResources().getColor(android.R.color.black));

            view.setBackgroundColor(getResources().getColor(android.R.color.white));

            if (position == selectedPosition) {
                view.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                textView.setTextColor(getResources().getColor(android.R.color.white));
            }

            return view;
        }
    }
}
