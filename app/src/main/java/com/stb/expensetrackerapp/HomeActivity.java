package com.stb.expensetrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class HomeActivity extends AppCompatActivity {
    private TextView pageNameTextView;
    LinearLayout transactionContainer;
    private AdView homeBannerAd, transactionBannerAd, calcAd;
    private InterstitialAd interstitialAd;
    Button editBalanceButton;
    List<Transaction> transactionList = new ArrayList<>();
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/6ca2de9dac3eab1f9308058a/latest/";
    private QuotesUtility quotesUtility;
    private TextView userNameTextView, quoteTextView;
    private LinearLayout recentTransactionsLayout;
    private FirebaseFirestore db;
    private CardView balanceCard, savingsCard, bankCreditedCard;
    Button seeAllTransactionsButton;
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

        MobileAds.initialize(this, initializationStatus -> {});

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            AdRequest adRequest = new AdRequest.Builder().build();
            if (R.id.nav_home == itemId) {
                loadHomeContent(mainContent);
                pageNameTextView.setText("Home");
                return true;
            } else if (R.id.nav_charts == itemId) {
                loadChartsContent(mainContent);
                pageNameTextView.setText("Tranactions");
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
        AdRequest adRequest2 = new AdRequest.Builder().build();
        interstitialAd.load(this,"ca-app-pub-6273147349635004/3991680540", adRequest2,
                new InterstitialAdLoadCallback(){
                    @Override
                    public void onAdLoaded(InterstitialAd interstitialAd) {
                        interstitialAd.show(HomeActivity.this);
                    }
                });
        userNameTextView = findViewById(R.id.greetingTextView);
        quoteTextView = findViewById(R.id.quoteTextView);
        balanceCard = findViewById(R.id.balanceCard);
        savingsCard = findViewById(R.id.savingsCard);
        bankCreditedCard = findViewById(R.id.bankCreditedCard);
        homeBannerAd = findViewById(R.id.homeBannerAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        homeBannerAd.loadAd(adRequest);

        quotesUtility = new QuotesUtility();
        String[] quotes = quotesUtility.getQuotes();
        String dailyQuote = getDailyQuote(quotes);
        quoteTextView.setText(dailyQuote);
        editBalanceButton = findViewById(R.id.editBalanceButton);
        editBalanceButton.setVisibility(View.GONE);

        getUserData();

        editBalanceButton.setVisibility(View.VISIBLE);
        editBalanceButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, recordTransaction.class);
            startActivity(intent);
        });
    }
    private void setupSettingsScreen(View settingsView) {
        AdRequest adRequest2 = new AdRequest.Builder().build();
        interstitialAd.load(this,"ca-app-pub-6273147349635004/3991680540", adRequest2,
                new InterstitialAdLoadCallback(){
                    @Override
                    public void onAdLoaded(InterstitialAd interstitialAd) {
                        interstitialAd.show(HomeActivity.this);
                    }
                });
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
                boolean notificationsEnabled = notificationSwitch.isChecked();

                Long ageValue = null;
                try {
                    if (!newAge.isEmpty()) {
                        ageValue = Long.parseLong(newAge);
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(HomeActivity.this, "Invalid age format. Please enter a valid number.", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("users").document(userId)
                        .update("firstName", newFirstName,
                                "lastName", newLastName,
                                "username", newUsername,
                                "dob", newDob,
                                "age", ageValue,
                                "currency", newCurrency,
                                "notificationsEnabled", notificationsEnabled)
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
        try {
            String apiUrl = API_URL + fromCurrency;
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(apiUrl).openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(5000); // 5 seconds timeout
            urlConnection.setReadTimeout(5000); // 5 seconds timeout

            InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());
            StringBuilder response = new StringBuilder();
            int charRead;
            while ((charRead = reader.read()) != -1) {
                response.append((char) charRead);
            }
            reader.close();
            Log.d("API Response", response.toString());

            // Parsing the response
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject rates = jsonResponse.getJSONObject("conversion_rates");

            if (rates.has(toCurrency)) {
                return rates.getDouble(toCurrency);
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    private double calculateInterest(double principal, double rate, int years) {
        return principal * rate * years / 100;
    }
    private void CalculationScreen(View calcView) {
        AdRequest adRequest2 = new AdRequest.Builder().build();
        interstitialAd.load(this,"ca-app-pub-6273147349635004/3991680540", adRequest2,
                new InterstitialAdLoadCallback(){
                    @Override
                    public void onAdLoaded(InterstitialAd interstitialAd) {
                        interstitialAd.show(HomeActivity.this);
                    }
                });
        calcAd = calcView.findViewById(R.id.calcAD);
        calcAd.loadAd(new AdRequest.Builder().build());
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
                new Thread(() -> {
                    double conversionRate = getConversionRate(fromCurrency, toCurrency);
                    if (conversionRate != -1) {
                        double convertedAmount = amount * conversionRate;
                        runOnUiThread(() -> {
                            conversionResultTextView.setText(String.format(Locale.getDefault(), "Converted Amount: %.2f %s", convertedAmount, toCurrency));
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(HomeActivity.this, "Error fetching conversion rate.", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            } else {
                Toast.makeText(HomeActivity.this, "Please enter an amount.", Toast.LENGTH_SHORT).show();
            }
        });

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
    private void tranactionsScreen(){
        AdRequest adRequest2 = new AdRequest.Builder().build();
        interstitialAd.load(this,"ca-app-pub-6273147349635004/3991680540", adRequest2,
                new InterstitialAdLoadCallback(){
                    @Override
                    public void onAdLoaded(InterstitialAd interstitialAd) {
                        interstitialAd.show(HomeActivity.this);
                    }
                });
        String userIdd = "currentUserId";
        transactionBannerAd = findViewById(R.id.transAds);
        transactionBannerAd.loadAd(new AdRequest.Builder().build());
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userIdd = currentUser.getUid();
        }
        final String UserIddd = userIdd;
        transactionContainer = findViewById(R.id.transactionContainer);
        db.collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    transactionList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Transaction transaction = document.toObject(Transaction.class);
                        if (transaction != null && transaction.getUserId().equals(UserIddd)) {
                            transactionList.add(transaction);
                        }
                    }
                    Map<String, List<Transaction>> groupedTransactions = groupTransactionsByDate(transactionList);
                    for (Map.Entry<String, List<Transaction>> entry : groupedTransactions.entrySet()) {
                        addHeader(entry.getKey());
                        for (Transaction transaction : entry.getValue()) {
                            addTransactionToUI(transaction);
                        }
                    }
                });
    }
    private void addTransactionToUI(Transaction transaction) {
        LinearLayout transactionLayout = new LinearLayout(this);
        transactionLayout.setOrientation(LinearLayout.VERTICAL);
        transactionLayout.setPadding(16, 16, 16, 16);
        transactionLayout.setBackgroundResource(R.drawable.transaction_card_background);

        transactionLayout.addView(createTextView("Amount: " + transaction.getCurrency() + " " + transaction.getAmount(), true));
        transactionLayout.addView(createTextView("Balance Type: " + transaction.getBalanceType(), true));

        String amountDetails = "Previous Amount: " + transaction.getPreviousAmount() + " -> Updated Amount: " + transaction.getUpdatedAmount();
        transactionLayout.addView(createTextView(amountDetails, false));

        if ("Add".equals(transaction.getOperation())) {
            transactionLayout.setBackgroundColor(Color.parseColor("#ADD8E6"));
        } else if ("Subtract".equals(transaction.getOperation())) {
            transactionLayout.setBackgroundColor(Color.parseColor("#90EE90"));
        }
        if (!"Completed".equals(transaction.getStatus())) {
            transactionLayout.setBackgroundColor(Color.parseColor("#FFCCCB"));
        }
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
        divider.setBackgroundColor(Color.parseColor("#D3D3D3"));

        transactionContainer.addView(transactionLayout);
        transactionContainer.addView(divider);
    }
    private void addHeader(String date) {
        TextView header = new TextView(this);
        header.setText(date);
        header.setTextSize(20);
        header.setTypeface(ResourcesCompat.getFont(this, R.font.raleway_semibold), Typeface.BOLD);  // Apply the custom font
        header.setPadding(16, 8, 16, 8);
        header.setGravity(Gravity.START);
        header.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        header.setTextColor(Color.WHITE);
        transactionContainer.addView(header);
    }
    private TextView createTextView(String text, boolean isBold) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(isBold ? 18 : 16);
        textView.setGravity(Gravity.START);
        textView.setPadding(8, 8, 8, 8);
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.raleway_semibold), isBold ? Typeface.BOLD : Typeface.NORMAL);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        return textView;
    }
    private Map<String, List<Transaction>> groupTransactionsByDate(List<Transaction> transactions) {
        Map<String, List<Transaction>> groupedTransactions = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        for (Transaction transaction : transactions) {
            long timestampMillis = Long.parseLong(transaction.getTimestamp());
            String date = sdf.format(new Date(timestampMillis));

            if (!groupedTransactions.containsKey(date)) {
                groupedTransactions.put(date, new ArrayList<>());
            }
            groupedTransactions.get(date).add(transaction);
        }

        return groupedTransactions;
    }
    private void loadChartsContent(RelativeLayout container) {
        container.removeAllViews();
        View chartsView = LayoutInflater.from(this).inflate(R.layout.content_transactions, container, false);
        container.addView(chartsView);
        tranactionsScreen();
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
        userNameTextView.setVisibility(View.GONE);
        balanceCard.setVisibility(View.GONE);
        savingsCard.setVisibility(View.GONE);
        bankCreditedCard.setVisibility(View.GONE);
        quoteTextView.setVisibility(View.GONE);
        editBalanceButton.setVisibility(View.GONE);
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
                                String currency = document.getString("currency");

                                TimeZone timeZone = TimeZone.getDefault();
                                Calendar calendar = Calendar.getInstance(timeZone);
                                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                                String greetingMessage = getGreetingBasedOnTime(hourOfDay);
                                userNameTextView.setText(greetingMessage + ", " + userName + "!");

                                String currencySymbol = getCurrencySymbol(currency);
                                TextView balanceTextView = balanceCard.findViewById(R.id.textViewInsideCardBalance);
                                TextView savingsTextView = savingsCard.findViewById(R.id.textViewInsideCardSavings);
                                TextView bankCreditedTextView = bankCreditedCard.findViewById(R.id.textViewInsideCardBankCredited);

                                balanceTextView.setText("Balance: " + currencySymbol + " " + balance);
                                savingsTextView.setText("Savings: " + currencySymbol + " " + savings);
                                bankCreditedTextView.setText("Cash: " + currencySymbol + " " + cash);

                                String balanceMessage = "Your balance represents the total amount of money you have in your account.";
                                String savingsMessage = "Your savings are the funds you've set aside for future use or investment.";
                                String cashMessage = "The cash amount reflects the physical money you currently have.";

                                TextView balanceHintTextView = balanceCard.findViewById(R.id.textViewBalanceHint);
                                balanceHintTextView.setText(balanceMessage);

                                TextView savingsHintTextView = savingsCard.findViewById(R.id.textViewSavingsHint);
                                savingsHintTextView.setText(savingsMessage);

                                TextView bankCreditedHintTextView = bankCreditedCard.findViewById(R.id.textViewBankCreditedHint);
                                bankCreditedHintTextView.setText(cashMessage);

                                userNameTextView.setVisibility(View.VISIBLE);
                                balanceCard.setVisibility(View.VISIBLE);
                                savingsCard.setVisibility(View.VISIBLE);
                                bankCreditedCard.setVisibility(View.VISIBLE);
                                quoteTextView.setVisibility(View.VISIBLE);
                                editBalanceButton.setVisibility(View.VISIBLE);
                            }
                        } else {
                        }
                        showProgress(false);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(HomeActivity.this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        showProgress(false);
                    });
        } else {
            Toast.makeText(HomeActivity.this, "No user is signed in.", Toast.LENGTH_SHORT).show();
            showProgress(false);
        }
    }
    private String getGreetingBasedOnTime(int hourOfDay) {
        if (hourOfDay >= 5 && hourOfDay < 12) {
            return "Good Morning";
        } else if (hourOfDay >= 12 && hourOfDay < 17) {
            return "Good Afternoon";
        } else if (hourOfDay >= 17 && hourOfDay < 21) {
            return "Good Evening";
        } else if (hourOfDay >= 21 && hourOfDay < 24) {
            return "Good Night";
        } else {
            return "Hello";
        }
    }
    private String getCurrencySymbol(String currencyCode) {
        switch (currencyCode) {
            case "PKR":
                return "₨";
            case "EUR":
                return "€";
            case "USD":
                return "$";
            case "INR":
                return "₹";
            case "GBP":
                return "£";
            default:
                return "";
        }
    }
    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
}
