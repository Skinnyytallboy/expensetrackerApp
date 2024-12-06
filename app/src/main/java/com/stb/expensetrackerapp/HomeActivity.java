package com.stb.expensetrackerapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class HomeActivity extends AppCompatActivity {

    private TextView textViewUsername, textViewFirstName, textViewLastName, textViewAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize the TextViews to display the user information
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewFirstName = findViewById(R.id.textViewFirstName);
        textViewLastName = findViewById(R.id.textViewLastName);
        textViewAge = findViewById(R.id.textViewAge);

        // Get current authenticated user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Fetch user data from Firestore
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Retrieve user data from Firestore
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Retrieve user data from the Firestore document
                            String username = documentSnapshot.getString("username");
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            Long age = documentSnapshot.getLong("age");

                            // Display user data in the TextViews
                            if (username != null) textViewUsername.setText(username);
                            if (firstName != null) textViewFirstName.setText(firstName);
                            if (lastName != null) textViewLastName.setText(lastName);
                            if (age != null) textViewAge.setText(String.valueOf(age));
                        } else {
                            Toast.makeText(HomeActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure while fetching data
                        Toast.makeText(HomeActivity.this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // If the user is not authenticated, display an error message
            Toast.makeText(HomeActivity.this, "No user is signed in.", Toast.LENGTH_SHORT).show();
        }
    }
}
