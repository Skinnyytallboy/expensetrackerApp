package com.stb.expensetrackerapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class addTransaction extends AppCompatActivity {

    private EditText etAmount, etNotes;
    private Spinner spCategory;
    private Button btnSaveTransaction;
    private RadioGroup transactionTypeGroup;

    private FirebaseFirestore db; // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Initialize UI elements
        etAmount = findViewById(R.id.etAmount);
        etNotes = findViewById(R.id.etNotes);
        spCategory = findViewById(R.id.spCategory);
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction);
        transactionTypeGroup = findViewById(R.id.transactionTypeGroup);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Populate category spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.transaction_categories, // Define categories in res/values/strings.xml
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        // Save transaction
        btnSaveTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTransactionToFirestore();
            }
        });
    }

    private void saveTransactionToFirestore() {
        String amountStr = etAmount.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();

        // Get transaction type
        int selectedTypeId = transactionTypeGroup.getCheckedRadioButtonId();
        String transactionType = "Expense"; // Default
        if (selectedTypeId == R.id.rbIncome) {
            transactionType = "Income";
        } else if (selectedTypeId == R.id.rbTransfer) {
            transactionType = "Transfer";
        }

        // Validate input
        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Amount is required!");
            return;
        }

        double amount = Double.parseDouble(amountStr);

        // Create transaction data
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("type", transactionType);
        transactionData.put("amount", amount);
        transactionData.put("category", category);
        transactionData.put("notes", notes);
        transactionData.put("timestamp", System.currentTimeMillis());

        // Save to Firestore
        db.collection("transactions")
                .add(transactionData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Transaction Saved!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving transaction!", Toast.LENGTH_SHORT).show();
                });
    }
}