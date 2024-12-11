package com.stb.expensetrackerapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class editBalance extends AppCompatActivity {

    TextView editBalanceTitle;
    TextView editAmountTitle;
    TextView balanceTypeTitle;
    TextView operationTitle;
    TextView categoryTitle;
    TextView descriptionTitle;

    private EditText amountInput;
    private EditText descriptionInput;
    private Spinner balanceTypeSpinner;
    private Spinner categorySpinner;
    private RadioGroup operationGroup;
    private Button confirmButton;
    private Button cancelButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private double bankBalance;
    private double cashAmount;
    private double savingsAmount;
    private String currencyType;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_balance);

        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        editBalanceTitle = findViewById(R.id.titleTextView);
        editAmountTitle = findViewById(R.id.editAmountTitle);
        balanceTypeTitle = findViewById(R.id.balanceTypeTitle);
        operationTitle = findViewById(R.id.operationTitle);
        categoryTitle = findViewById(R.id.categoryTitle);
        descriptionTitle = findViewById(R.id.descriptionTitle);

        amountInput = findViewById(R.id.amountInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        balanceTypeSpinner = findViewById(R.id.balanceTypeSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);
        operationGroup = findViewById(R.id.operationGroup);
        confirmButton = findViewById(R.id.confirmButton);
        cancelButton = findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(v -> finish());

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



    private void showSnackbar(String message, boolean isError) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        if (isError) {
            snackbar.setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            snackbar.setBackgroundTint(getResources().getColor(android.R.color.holo_green_dark));
        }
        snackbar.show();
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
                            currencyType = documentSnapshot.getString("currency");
                        } else {
                            showSnackbar("User data not found, please try again later.", true);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", e.getMessage(), e);
                        showSnackbar("Error fetching balances: " + e.getMessage(), true);
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
        progressBar.setVisibility(View.VISIBLE);
        toggleUiVisibility(false);

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        String amountStr = amountInput.getText().toString();
        if (amountStr.isEmpty()) {
            showSnackbar("Please enter an amount to proceed.", true);
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String balanceType = balanceTypeSpinner.getSelectedItem().toString();
        int selectedOperationId = operationGroup.getCheckedRadioButtonId();
        RadioButton selectedOperation = findViewById(selectedOperationId);

        if (selectedOperation == null) {
            showSnackbar("Please select a valid operation (Add or Subtract).", true);
            return;
        }

        String operation = selectedOperation.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();
        String description = descriptionInput.getText().toString().trim();

        if ("Add".equals(operation)) {
            updateBalance(balanceType, amount, true, category, description);
        } else if ("Subtract".equals(operation)) {
            updateBalance(balanceType, amount, false, category, description);
        }
    }

    private void updateBalance(String balanceType, double amount, boolean isAdding, String category, String description) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            showSnackbar("User not authenticated. Please log in again.", true);
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
            showSnackbar("Operation would result in negative balance. Please check again.", true);
            return;
        }

        Map<String, Object> balanceData = new HashMap<>();
        balanceData.put("bankBalance", String.valueOf(bankBalance));
        balanceData.put("cashAmount", String.valueOf(cashAmount));
        balanceData.put("savingsAmount", String.valueOf(savingsAmount));

        db.collection("users").document(userId)
                .update(balanceData)
                .addOnSuccessListener(aVoid -> {
                    showSnackbar("Balance updated successfully.", false);
                    toggleUiVisibility(true);
                    recordTransaction(balanceType, amount, isAdding, category, description, previousAmount, updatedAmount);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", e.getMessage(), e);
                    toggleUiVisibility(true);
                    showSnackbar("Error updating balance: " + e.getMessage(), true);
                });
    }

    private void recordTransaction(String balanceType, double amount, boolean isAdding, String category, String description, double previousAmount, double updatedAmount) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            showSnackbar("User not authenticated. Please log in again.", true);
            return;
        }

        String userId = currentUser.getUid();
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("transactionId", UUID.randomUUID().toString());
        transactionData.put("userId", userId);
        transactionData.put("balanceType", balanceType);
        transactionData.put("amount", String.valueOf(amount));
        transactionData.put("operation", isAdding ? "Add" : "Subtract");
        transactionData.put("category", category);
        transactionData.put("description", description.isEmpty() ? "No description provided" : description);
        transactionData.put("previousAmount", String.valueOf(previousAmount));
        transactionData.put("updatedAmount", String.valueOf(updatedAmount));
        transactionData.put("currency", currencyType);
        transactionData.put("timestamp", String.valueOf(System.currentTimeMillis()));
        transactionData.put("status", "Completed");

        db.collection("transactions")
                .add(transactionData)
                .addOnSuccessListener(documentReference -> showSnackbar("Transaction recorded successfully.", false))
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", e.getMessage(), e);
                    showSnackbar("Error recording transaction: " + e.getMessage(), true);
                });
    }

    private void toggleUiVisibility(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        editAmountTitle.setVisibility(visibility);
        balanceTypeTitle.setVisibility(visibility);
        operationTitle.setVisibility(visibility);
        categoryTitle.setVisibility(visibility);
        descriptionTitle.setVisibility(visibility);
        amountInput.setVisibility(visibility);
        descriptionInput.setVisibility(visibility);
        balanceTypeSpinner.setVisibility(visibility);
        categorySpinner.setVisibility(visibility);
        operationGroup.setVisibility(visibility);
        confirmButton.setVisibility(visibility);
        cancelButton.setVisibility(visibility);

        if (show) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
