package com.stb.expensetrackerapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        RelativeLayout mainContent = findViewById(R.id.mainContent);
        loadHomeContent(mainContent);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (R.id.nav_home == itemId) {
                loadHomeContent(mainContent);
                return true;
            } else if (R.id.nav_charts == itemId) {
                loadChartsContent(mainContent);
                return true;
            } else if (R.id.nav_transactions == itemId) {
                loadTransactionsContent(mainContent);
                return true;
            } else if (R.id.nav_calc == itemId) {
                loadCalculatorContent(mainContent);
                return true;
            } else if (R.id.nav_settings == itemId) {
                loadSettingsContent(mainContent);
                return true;
            } else {
                return false;
            }
        });
    }

    private void loadHomeContent(RelativeLayout container) {
        container.removeAllViews();
        View homeView = LayoutInflater.from(this).inflate(R.layout.content_home, container, false);
        container.addView(homeView);
    }

    private void loadChartsContent(RelativeLayout container) {
        container.removeAllViews();
        View chartsView = LayoutInflater.from(this).inflate(R.layout.content_charts, container, false);
        container.addView(chartsView);
    }

    private void loadTransactionsContent(RelativeLayout container) {
        container.removeAllViews();
        View transactionsView = LayoutInflater.from(this).inflate(R.layout.content_transactions, container, false);
        container.addView(transactionsView);
    }

    private void loadCalculatorContent(RelativeLayout container) {
        container.removeAllViews();
        View calcView = LayoutInflater.from(this).inflate(R.layout.content_calculator, container, false);
        container.addView(calcView);
    }

    private void loadSettingsContent(RelativeLayout container) {
        container.removeAllViews();
        View settingsView = LayoutInflater.from(this).inflate(R.layout.content_settings, container, false);
        container.addView(settingsView);
    }
}
