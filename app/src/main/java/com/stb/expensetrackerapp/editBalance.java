package com.stb.expensetrackerapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class editBalance extends AppCompatActivity {

    private EditText amountInput;
    private Spinner balanceTypeSpinner;
    private Spinner categorySpinner;
    private RadioGroup operationGroup;
    private Button confirmButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private double bankBalance;
    private double cashAmount;
    private double savingsAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_balance);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        amountInput = findViewById(R.id.amountInput);
        balanceTypeSpinner = findViewById(R.id.balanceTypeSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);
        operationGroup = findViewById(R.id.operationGroup);
        confirmButton = findViewById(R.id.confirmButton);

        setupBalanceTypeSpinner();
        setupCategorySpinner();

        confirmButton.setOnClickListener(v -> processOperation());

        fetchInitialBalances();
    }

    private void setupBalanceTypeSpinner() {
        ArrayList<String> balanceTypes = new ArrayList<>();
        balanceTypes.add("Bank Balance");
        balanceTypes.add("Cash Amount");
        balanceTypes.add("Savings Amount");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, balanceTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        balanceTypeSpinner.setAdapter(adapter);
    }

    private void setupCategorySpinner() {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Salary");
        categories.add("Pocket Money");
        categories.add("Pension");
        categories.add("Freelancing");
        categories.add("Gifts");
        categories.add("Investment Returns");
        categories.add("Loan Repayment");
        categories.add("Groceries");
        categories.add("Utilities");
        categories.add("Rent");
        categories.add("Transportation");
        categories.add("Entertainment");
        categories.add("Health & Fitness");
        categories.add("Charity");
        categories.add("Miscellaneous");
        categories.add("Other");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void fetchInitialBalances() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            bankBalance = getDoubleValue(documentSnapshot, "bankBalance");
                            cashAmount = getDoubleValue(documentSnapshot, "cashAmount");
                            savingsAmount = getDoubleValue(documentSnapshot, "savingsAmount");
                        } else {
                            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", e.getMessage(), e);
                        Toast.makeText(this, "Error fetching balances: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private double getDoubleValue(DocumentSnapshot documentSnapshot, String field) {
        Object value = documentSnapshot.get(field);
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else {
            return 0.0;
        }
    }

    private void processOperation() {
        String amountStr = amountInput.getText().toString();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String balanceType = balanceTypeSpinner.getSelectedItem().toString();
        int selectedOperationId = operationGroup.getCheckedRadioButtonId();
        RadioButton selectedOperation = findViewById(selectedOperationId);

        if (selectedOperation == null) {
            Toast.makeText(this, "Please select an operation", Toast.LENGTH_SHORT).show();
            return;
        }

        String operation = selectedOperation.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();

        if ("Add".equals(operation)) {
            updateBalance(balanceType, amount, true, category);
        } else if ("Subtract".equals(operation)) {
            updateBalance(balanceType, amount, false, category);
        }
    }

    private void updateBalance(String balanceType, double amount, boolean isAdding, String category) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        boolean isValidOperation = true;
        final double previousAmount;
        final double updatedAmount;

        switch (balanceType) {
            case "Bank Balance":
                previousAmount = bankBalance;
                bankBalance += isAdding ? amount : -amount;
                updatedAmount = bankBalance;
                if (bankBalance < 0) isValidOperation = false;
                break;
            case "Cash Amount":
                previousAmount = cashAmount;
                cashAmount += isAdding ? amount : -amount;
                updatedAmount = cashAmount;
                if (cashAmount < 0) isValidOperation = false;
                break;
            case "Savings Amount":
                previousAmount = savingsAmount;
                savingsAmount += isAdding ? amount : -amount;
                updatedAmount = savingsAmount;
                if (savingsAmount < 0) isValidOperation = false;
                break;
            default:
                previousAmount = 0.0;
                updatedAmount = 0.0;
                isValidOperation = false;
                break;
        }

        if (!isValidOperation) {
            Toast.makeText(this, "Operation would result in negative balance", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> balanceData = new HashMap<>();
        balanceData.put("bankBalance", String.valueOf(bankBalance));
        balanceData.put("cashAmount", String.valueOf(cashAmount));
        balanceData.put("savingsAmount", String.valueOf(savingsAmount));

        db.collection("users").document(userId)
                .update(balanceData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Balance updated successfully", Toast.LENGTH_SHORT).show();
                    recordTransaction(balanceType, amount, isAdding, category, previousAmount, updatedAmount);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", e.getMessage(), e);
                    Toast.makeText(this, "Error updating balance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void recordTransaction(String balanceType, double amount, boolean isAdding, String category, double previousAmount, double updatedAmount) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("userId", userId);
        transactionData.put("balanceType", balanceType);
        transactionData.put("amount", String.valueOf(amount));
        transactionData.put("operation", isAdding ? "Add" : "Subtract");
        transactionData.put("category", category);
        transactionData.put("previousAmount", String.valueOf(previousAmount));
        transactionData.put("updatedAmount", String.valueOf(updatedAmount));
        transactionData.put("timestamp", String.valueOf(System.currentTimeMillis()));

        db.collection("transactions")
                .add(transactionData)
                .addOnSuccessListener(documentReference -> Toast.makeText(this, "Transaction recorded successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", e.getMessage(), e);
                    Toast.makeText(this, "Error recording transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
