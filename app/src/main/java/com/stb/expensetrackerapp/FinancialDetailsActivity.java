package com.stb.expensetrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FinancialDetailsActivity extends AppCompatActivity {

    private EditText etCashAmount, etBankBalance, etSavingsAmount;
    private Button btnSaveFinancialDetails;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_details);

        db = FirebaseFirestore.getInstance();

        etCashAmount = findViewById(R.id.etCashAmount);
        etBankBalance = findViewById(R.id.etBankBalance);
        etSavingsAmount = findViewById(R.id.etSavingsAmount);
        btnSaveFinancialDetails = findViewById(R.id.btnSaveFinancialDetails);

        btnSaveFinancialDetails.setOnClickListener(v -> {
            String cashAmount = etCashAmount.getText().toString().trim();
            String bankBalance = etBankBalance.getText().toString().trim();
            String savingsAmount = etSavingsAmount.getText().toString().trim();

            if (cashAmount.isEmpty() || bankBalance.isEmpty() || savingsAmount.isEmpty()) {
                Toast.makeText(FinancialDetailsActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                db.collection("users").document(userId)
                        .update("cashAmount", cashAmount, "bankBalance", bankBalance, "savingsAmount", savingsAmount)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(FinancialDetailsActivity.this, "Financial details saved successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(FinancialDetailsActivity.this, MainActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(FinancialDetailsActivity.this, "Error saving details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}
