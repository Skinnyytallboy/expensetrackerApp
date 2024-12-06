package com.stb.expensetrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

public class SignupActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etUsername, etFirstName, etLastName, etAge;
    private Button btnSignup;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etAge = findViewById(R.id.etAge);
        btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String age = etAge.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || age.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                String userId = mAuth.getCurrentUser().getUid();

                                User user = new User(username, firstName, lastName, Integer.parseInt(age));

                                db.collection("users")
                                        .document(userId)
                                        .set(user)
                                        .addOnSuccessListener(aVoid -> {
                                            startActivity(new Intent(SignupActivity.this, CurrencySelectionActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(SignupActivity.this, "Error saving user info: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            } else {
                                // Handle sign-up failure
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(SignupActivity.this, "Signup failed: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    // User class to store user details
    public static class User {
        private String username;
        private String firstName;
        private String lastName;
        private int age;

        public User(String username, String firstName, String lastName, int age) {
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
        }

        public String getUsername() {
            return username;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public int getAge() {
            return age;
        }
    }
}
