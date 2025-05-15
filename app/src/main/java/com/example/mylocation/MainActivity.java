package com.example.mylocation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private GridLayout busGridLayout;
    private static final String SHARED_PREFS = "bus_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        busGridLayout = findViewById(R.id.gridLayout);
        setupBusButtons();
    }

    private void setupBusButtons() {
        int totalBuses = 30;

        for (int i = 1; i <= totalBuses; i++) {
            Button busButton = new Button(this);
            busButton.setText("Bus " + i);
            busButton.setTag("Bus " + i);

            // Set button's initial appearance based on its saved state
            updateButtonAppearance(busButton, i);

            final int busNumber = i;
            busButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, BusControlActivity.class);
                intent.putExtra("BUS_NUMBER", busNumber);
                startActivity(intent);
            });

            // Set LayoutParams to center the button and add margins
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = GridLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.setMargins(16, 16, 16, 16);  // Add spacing between buttons
            busButton.setLayoutParams(layoutParams);

            busGridLayout.addView(busButton);
        }
    }

    private void updateButtonAppearance(Button busButton, int busNumber) {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        boolean isSharing = prefs.getBoolean("BUS_" + busNumber + "_SHARING", false);

        if (isSharing) {
            busButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBusSharing));
        } else {
            busButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBusIdle));
        }
    }
}
