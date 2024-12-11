package com.stb.expensetrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView pageNameTextView;

    // content_home.xml
    private QuotesUtility quotesUtility;
    private TextView userNameTextView, quoteTextView;
    private LinearLayout recentTransactionsLayout;
    private FirebaseFirestore db;
    private CardView balanceCard, savingsCard, bankCreditedCard;

    // Progress bar
    private RelativeLayout overlay;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        overlay = findViewById(R.id.overlay);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        pageNameTextView = findViewById(R.id.pageNameTextView);

        RelativeLayout mainContent = findViewById(R.id.mainContent);
        loadHomeContent(mainContent);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (R.id.nav_home == itemId) {
                loadHomeContent(mainContent);
                pageNameTextView.setText("Home");
                return true;
            } else if (R.id.nav_charts == itemId) {
                loadChartsContent(mainContent);
                pageNameTextView.setText("Charts");
                return true;
            } else if (R.id.nav_cards == itemId) {
                loadCardsContent(mainContent);
                pageNameTextView.setText("Cards Info");
                return true;
            } else if (R.id.nav_calc == itemId) {
                loadCalculatorContent(mainContent);
                pageNameTextView.setText("Calculations");
                return true;
            } else if (R.id.nav_settings == itemId) {
                loadSettingsContent(mainContent);
                pageNameTextView.setText("Settings");
                return true;
            } else {
                return false;
            }
        });

    }

    private void homeScreen(){
        userNameTextView = findViewById(R.id.userNameTextView);
        quoteTextView = findViewById(R.id.quoteTextView);
        balanceCard = findViewById(R.id.balanceCard);
        savingsCard = findViewById(R.id.savingsCard);
        bankCreditedCard = findViewById(R.id.bankCreditedCard);
        recentTransactionsLayout = findViewById(R.id.recentTransactionsLayout);

        quotesUtility = new QuotesUtility();
        String[] quotes = quotesUtility.getQuotes();
        String dailyQuote = getDailyQuote(quotes);
        quoteTextView.setText(dailyQuote);

        getUserData();

        Button editBalanceButton = findViewById(R.id.editBalanceButton);
        editBalanceButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, editBalance.class);
            startActivity(intent);
        });

        Button addNewTransactionButton = findViewById(R.id.addNewTransactionButton);
        addNewTransactionButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, addTransaction.class);
            startActivity(intent);
        });
    }

    private void setupSettingsScreen(View settingsView) {
        EditText firstNameEditText = settingsView.findViewById(R.id.firstNameEditText);
        EditText lastNameEditText = settingsView.findViewById(R.id.lastNameEditText);
        EditText usernameEditText = settingsView.findViewById(R.id.usernameEditText);
        EditText dobEditText = settingsView.findViewById(R.id.dobEditText);
        EditText emailEditText = settingsView.findViewById(R.id.emailEditText);
        EditText ageEditText = settingsView.findViewById(R.id.ageEditText);
        Spinner currencySpinner = settingsView.findViewById(R.id.currencySpinner);
        Button saveSettingsButton = settingsView.findViewById(R.id.saveSettingsButton);

        // New UI elements
        Switch notificationSwitch = settingsView.findViewById(R.id.notificationSwitch); // Notification preferences switch
        Button resetPasswordButton = settingsView.findViewById(R.id.resetPasswordButton); // Reset password button

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Load existing data
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            firstNameEditText.setText(documentSnapshot.getString("firstName"));
                            lastNameEditText.setText(documentSnapshot.getString("lastName"));
                            usernameEditText.setText(documentSnapshot.getString("username"));
                            dobEditText.setText(documentSnapshot.getString("dob"));
                            emailEditText.setText(currentUser.getEmail());

                            // Retrieve 'age' as Long (handle type mismatch)
                            Long age = documentSnapshot.getLong("age");
                            if (age != null) {
                                ageEditText.setText(String.valueOf(age));  // Set age as string in the EditText
                            } else {
                                ageEditText.setText("");  // If no age is set, clear the EditText
                            }

                            // Set currency spinner selection
                            String currency = documentSnapshot.getString("currency");
                            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currency_options, android.R.layout.simple_spinner_item);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            currencySpinner.setAdapter(adapter);
                            if (currency != null) {
                                int position = adapter.getPosition(currency);
                                currencySpinner.setSelection(position);
                            }

                            // Load notification preference from Firestore
                            Boolean notificationsEnabled = documentSnapshot.getBoolean("notificationsEnabled");

                            // Default to false if 'notificationsEnabled' is null
                            if (notificationsEnabled != null) {
                                notificationSwitch.setChecked(notificationsEnabled);
                            } else {
                                notificationSwitch.setChecked(false); // Default to false if null
                            }
                        }
                    });
        }

        // Save changes
        saveSettingsButton.setOnClickListener(v -> {
            if (currentUser != null) {
                String userId = currentUser.getUid();
                String newFirstName = firstNameEditText.getText().toString();
                String newLastName = lastNameEditText.getText().toString();
                String newUsername = usernameEditText.getText().toString();
                String newDob = dobEditText.getText().toString();
                String newEmail = emailEditText.getText().toString();
                String newAge = ageEditText.getText().toString();
                String newCurrency = currencySpinner.getSelectedItem().toString();
                boolean notificationsEnabled = notificationSwitch.isChecked();  // Get notification preference state

                // Convert age to Long before saving (make sure it's a number)
                Long ageValue = null;
                try {
                    if (!newAge.isEmpty()) {
                        ageValue = Long.parseLong(newAge);
                    }
                } catch (NumberFormatException e) {
                    // Handle the case where the age is not a valid number
                    Toast.makeText(HomeActivity.this, "Invalid age format. Please enter a valid number.", Toast.LENGTH_SHORT).show();
                    return;  // Prevent saving if age is invalid
                }

                // Update database with new settings
                db.collection("users").document(userId)
                        .update("firstName", newFirstName,
                                "lastName", newLastName,
                                "username", newUsername,
                                "dob", newDob,
                                "age", ageValue, // Save the age as a Long
                                "currency", newCurrency,
                                "notificationsEnabled", notificationsEnabled)  // Save notification preference
                        .addOnSuccessListener(aVoid -> Toast.makeText(HomeActivity.this, "Settings updated!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "Error updating settings: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                currentUser.updateEmail(newEmail)
                        .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "Failed to update email: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            }
        });

        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            if (!email.isEmpty()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(HomeActivity.this, "Password reset link sent!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(HomeActivity.this, "Error sending password reset: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(HomeActivity.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double getConversionRate(String fromCurrency, String toCurrency) {
        // Implement logic to get conversion rate (e.g., from an API or a predefined map)
        return 1.0; // Placeholder
    }

    private double calculateInterest(double principal, double rate, int years) {
        return principal * rate * years / 100;
    }
    private void CalculationScreen(View calcView) {
        EditText amountEditText = calcView.findViewById(R.id.amountEditText);
        AutoCompleteTextView fromCurrencySpinner = calcView.findViewById(R.id.fromCurrencySpinner);  // Change to AutoCompleteTextView
        AutoCompleteTextView toCurrencySpinner = calcView.findViewById(R.id.toCurrencySpinner);  // Change to AutoCompleteTextView
        Button convertButton = calcView.findViewById(R.id.convertButton);
        TextView conversionResultTextView = calcView.findViewById(R.id.conversionResultTextView);
        EditText interestRateEditText = calcView.findViewById(R.id.interestRateEditText);
        EditText yearsEditText = calcView.findViewById(R.id.yearsEditText);
        Button calculateInterestButton = calcView.findViewById(R.id.calculateInterestButton);
        TextView interestResultTextView = calcView.findViewById(R.id.interestResultTextView);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currency_options, android.R.layout.simple_dropdown_item_1line);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        fromCurrencySpinner.setAdapter(adapter);
        toCurrencySpinner.setAdapter(adapter);

        convertButton.setOnClickListener(v -> {
            String amountStr = amountEditText.getText().toString();
            if (!amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                String fromCurrency = fromCurrencySpinner.getText().toString();
                String toCurrency = toCurrencySpinner.getText().toString();
                double conversionRate = getConversionRate(fromCurrency, toCurrency);
                double convertedAmount = amount * conversionRate;
                conversionResultTextView.setText(String.format(Locale.getDefault(), "Converted Amount: %.2f %s", convertedAmount, toCurrency));
            } else {
                Toast.makeText(HomeActivity.this, "Please enter an amount.", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle interest calculation
        calculateInterestButton.setOnClickListener(v -> {
            String amountStr = amountEditText.getText().toString();
            String rateStr = interestRateEditText.getText().toString();
            String yearsStr = yearsEditText.getText().toString();
            if (!amountStr.isEmpty() && !rateStr.isEmpty() && !yearsStr.isEmpty()) {
                double principal = Double.parseDouble(amountStr);
                double rate = Double.parseDouble(rateStr);
                int years = Integer.parseInt(yearsStr);
                double interest = calculateInterest(principal, rate, years); // Implement this method
                interestResultTextView.setText(String.format(Locale.getDefault(), "Interest: %.2f", interest));
            } else {
                Toast.makeText(HomeActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHomeContent(RelativeLayout container) {
        container.removeAllViews();
        View homeView = LayoutInflater.from(this).inflate(R.layout.content_home, container, false);
        container.addView(homeView);
        homeScreen();
    }

    private void loadChartsContent(RelativeLayout container) {
        container.removeAllViews();
        View chartsView = LayoutInflater.from(this).inflate(R.layout.content_charts, container, false);
        container.addView(chartsView);
    }

    private void loadCardsContent(RelativeLayout container) {
        container.removeAllViews();
        View cardsView = LayoutInflater.from(this).inflate(R.layout.content_cards, container, false);
        container.addView(cardsView);
    }

    private void loadCalculatorContent(RelativeLayout container) {
        container.removeAllViews();
        View calcView = LayoutInflater.from(this).inflate(R.layout.content_calculator, container, false);
        container.addView(calcView);
        CalculationScreen(calcView);
    }

    private void loadSettingsContent(RelativeLayout container) {
        container.removeAllViews();
        View settingsView = LayoutInflater.from(this).inflate(R.layout.content_settings, container, false);
        container.addView(settingsView);
        setupSettingsScreen(settingsView);
    }

    private String getDailyQuote(String[] quotes) {
        SharedPreferences preferences = getSharedPreferences("QuotesPref", MODE_PRIVATE);
        int lastIndex = preferences.getInt("lastQuoteIndex", -1);
        String lastDate = preferences.getString("lastDate", "");
        String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        if (!currentDate.equals(lastDate)) {
            lastIndex = (lastIndex + 1) % quotes.length;
            preferences.edit()
                    .putInt("lastQuoteIndex", lastIndex)
                    .putString("lastDate", currentDate)
                    .apply();
        }
        return quotes[lastIndex == -1 ? 0 : lastIndex];
    }

    private void getUserData() {
        showProgress(true);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String userName = document.getString("username");
                                String balance = document.getString("bankBalance");
                                String savings = document.getString("savingsAmount");
                                String cash = document.getString("cashAmount");

                                userNameTextView.setText(userName);

                                TextView balanceTextView = balanceCard.findViewById(R.id.textViewInsideCardBalance);
                                TextView savingsTextView = savingsCard.findViewById(R.id.textViewInsideCardSavings);
                                TextView bankCreditedTextView = bankCreditedCard.findViewById(R.id.textViewInsideCardBankCredited);

                                balanceTextView.setText("Balance: " + balance);
                                savingsTextView.setText("Savings: " + savings);
                                bankCreditedTextView.setText("Cash: " + cash);

                                getRecentTransactions(userId);
                            }
                        } else {
                        }
                        showProgress(true);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(HomeActivity.this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        showProgress(true);
                    });
        } else {
            Toast.makeText(HomeActivity.this, "No user is signed in.", Toast.LENGTH_SHORT).show();
            showProgress(true);
        }
    }

    private void getRecentTransactions(String userId) {
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String transactionDetail = document.getString("detail");
                            String amount = document.getString("amount");

                            TextView transactionText = new TextView(this);
                            transactionText.setText(transactionDetail + ": " + amount);
                            transactionText.setPadding(16, 16, 16, 16);
                            recentTransactionsLayout.addView(transactionText);
                        }
                    } else {
                        // Handle error
                    }
                });
    }
    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            overlay.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            overlay.setVisibility(View.GONE);
        }
    }


}
