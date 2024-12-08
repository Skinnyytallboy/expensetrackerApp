package com.stb.expensetrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {

    // TODO: fix "something went wrong" error
    //  and to make the first letter of the first name and last name uppercase
    //  and to modify the progressbar.

    private TextView tvCreateAcc;
    private EditText etEmail, etPassword, etUsername, etFirstName, etLastName, etAge;
    private Button btnSignup;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isLoggedIn = false;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 16;
    private static final int MINIMUM_AGE = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvCreateAcc = findViewById(R.id.tvCreateAcc);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etAge = findViewById(R.id.etAge);
        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.progressBar);

        btnSignup.setOnClickListener(v -> {
            hideKeyboard();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String ageText = etAge.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || ageText.isEmpty()) {
                showSnackbar("Please enter all fields");
                return;
            }

            if (!isValidEmail(email)) {
                showSnackbar("Invalid email format");
                return;
            }

            if (!isValidPasswordLength(password)) {
                showSnackbar("Password must be between " + PASSWORD_MIN_LENGTH + " and " + PASSWORD_MAX_LENGTH + " characters");
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageText);
            } catch (NumberFormatException e) {
                showSnackbar("Invalid age entered");
                return;
            }

            if (age < MINIMUM_AGE) {
                showSnackbar("You must be at least " + MINIMUM_AGE + " years old to sign up");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            etEmail.setVisibility(View.GONE);
            etPassword.setVisibility(View.GONE);
            etUsername.setVisibility(View.GONE);
            etFirstName.setVisibility(View.GONE);
            etLastName.setVisibility(View.GONE);
            etAge.setVisibility(View.GONE);
            btnSignup.setVisibility(View.GONE);
            tvCreateAcc.setVisibility(View.GONE);
            checkIfEmailExists(email, () -> checkIfUsernameExists(username, () -> createUser(email, password, username, firstName, lastName, age)));
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(btnSignup.getWindowToken(), 0);
        }
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPasswordLength(String password) {
        return password.length() >= PASSWORD_MIN_LENGTH && password.length() <= PASSWORD_MAX_LENGTH;
    }

    private void checkIfEmailExists(String email, Runnable onSuccess) {
        db.collection("users").whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        etEmail.setVisibility(View.VISIBLE);
                        etPassword.setVisibility(View.VISIBLE);
                        etUsername.setVisibility(View.VISIBLE);
                        etFirstName.setVisibility(View.VISIBLE);
                        etLastName.setVisibility(View.VISIBLE);
                        etAge.setVisibility(View.VISIBLE);
                        btnSignup.setVisibility(View.VISIBLE);
                        tvCreateAcc.setVisibility(View.VISIBLE);
                        showSnackbar("Email already in use");
                    } else {
                        onSuccess.run();
                    }
                })
                .addOnFailureListener(e -> handleFirestoreError("Error checking email", e));
    }

    private void checkIfUsernameExists(String username, Runnable onSuccess) {
        db.collection("users").whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        etEmail.setVisibility(View.VISIBLE);
                        etPassword.setVisibility(View.VISIBLE);
                        etUsername.setVisibility(View.VISIBLE);
                        etFirstName.setVisibility(View.VISIBLE);
                        etLastName.setVisibility(View.VISIBLE);
                        etAge.setVisibility(View.VISIBLE);
                        btnSignup.setVisibility(View.VISIBLE);
                        tvCreateAcc.setVisibility(View.VISIBLE);
                        showSnackbar("Username already in use");
                    } else {
                        onSuccess.run();
                    }
                })
                .addOnFailureListener(e -> handleFirestoreError("Error checking username", e));
    }

    private void createUser(String email, String password, String username, String firstName, String lastName, int age) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    etEmail.setVisibility(View.VISIBLE);
                    etPassword.setVisibility(View.VISIBLE);
                    etUsername.setVisibility(View.VISIBLE);
                    etFirstName.setVisibility(View.VISIBLE);
                    etLastName.setVisibility(View.VISIBLE);
                    etAge.setVisibility(View.VISIBLE);
                    btnSignup.setVisibility(View.VISIBLE);
                    tvCreateAcc.setVisibility(View.VISIBLE);
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        User user = new User(username, firstName, lastName, age, email);
                        db.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    isLoggedIn = true;
                                    showSnackbar("Congratulations! Your account has been created successfully.");
                                    btnSignup.postDelayed(() -> {
                                        startActivity(new Intent(SignupActivity.this, CurrencySelectionActivity.class));
                                        finish();
                                    }, 2000);
                                })
                                .addOnFailureListener(e -> handleFirestoreError("Error saving user info", e));
                    } else {
                        String errorMessage = task.getException().getMessage();
                        if (!isLoggedIn)
                            showSnackbar("Signup failed: " + errorMessage);
                    }
                });
    }

    private void handleFirestoreError(String message, Exception e) {
        Log.e("FirestoreError", message + ": " + e.getMessage());
        //if (!isLoggedIn)
        //   showSnackbar("Something went wrong. Please try again.");
        progressBar.setVisibility(View.GONE);
        etEmail.setVisibility(View.VISIBLE);
        etPassword.setVisibility(View.VISIBLE);
        etUsername.setVisibility(View.VISIBLE);
        etFirstName.setVisibility(View.VISIBLE);
        etLastName.setVisibility(View.VISIBLE);
        etAge.setVisibility(View.VISIBLE);
        btnSignup.setVisibility(View.VISIBLE);
        tvCreateAcc.setVisibility(View.VISIBLE);
    }

    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public static class User {
        private String username;
        private String firstName;
        private String lastName;
        private int age;
        private String email;

        public User(String username, String firstName, String lastName, int age, String email) {
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.email = email;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
