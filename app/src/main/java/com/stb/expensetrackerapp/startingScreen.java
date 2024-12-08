package com.stb.expensetrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;

import android.Manifest;
import android.content.pm.PackageManager;

public class startingScreen extends AppCompatActivity {

    private ImageView ssImageHolder;
    private TextView errorText;
    private Button getStartedButton;
    private String[] images = {"start0", "start1", "start2", "start3"};
    private int imageIndex = 0;
    private Handler handler = new Handler();
    private Runnable imageSwitcher;
    private Animation fadeIn, fadeOut;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ExpenseTrackerPrefs";
    private static final String FIRST_LAUNCH_KEY = "isFirstLaunch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting_screen);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstLaunch = sharedPreferences.getBoolean(FIRST_LAUNCH_KEY, true);

        if (!isFirstLaunch) {
            proceedToOnboarding();
            return;
        }

        ssImageHolder = findViewById(R.id.ssImageHolder);
        errorText = findViewById(R.id.errorText);

        fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        getStartedButton = findViewById(R.id.getStartedButton);

        startImageSlideshow();

        getStartedButton.setOnClickListener(v -> {
            if (checkInternetConnection()) {
                sharedPreferences.edit().putBoolean(FIRST_LAUNCH_KEY, false).apply();
                requestPermissions();
            } else {
                showSnackbar("No internet connection. Please connect to the internet and try again.");
            }
        });
    }

    private void startImageSlideshow() {
        imageSwitcher = new Runnable() {
            @Override
            public void run() {
                ssImageHolder.startAnimation(fadeOut);
                handler.postDelayed(() -> {
                    ssImageHolder.setImageResource(getResources().getIdentifier(images[imageIndex], "drawable", getPackageName()));
                    ssImageHolder.startAnimation(fadeIn);
                    imageIndex = (imageIndex + 1) % images.length;
                }, fadeOut.getDuration());
                handler.postDelayed(this, 4000);
            }
        };
        handler.post(imageSwitcher);
    }

    private boolean checkInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
        }
        return false;
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.INTERNET
            }, 1);
        } else {
            proceedToOnboarding();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            proceedToOnboarding();
        } else {
            showSnackbar("Permissions denied. Cannot proceed.");
        }
    }

    private void proceedToOnboarding() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
}
