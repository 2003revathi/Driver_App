package com.example.mylocation;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BusControlActivity extends AppCompatActivity {

    private static final String SHARED_PREFS = "bus_prefs";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Switch sharingSwitch;
    private Button startButton;
    private Button stopButton;
    private TextView locationTextView;
    private int busNumber;
    private boolean isSharing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_control);

        sharingSwitch = findViewById(R.id.sharingSwitch);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        locationTextView = findViewById(R.id.locationTextView);

        busNumber = getIntent().getIntExtra("BUS_NUMBER", 1);

        setupButtons();
        updateUIFromPreferences();
    }

    private void setupButtons() {
        startButton.setOnClickListener(v -> {
            if (checkLocationPermission()) {
                startLocationSharing();
            } else {
                requestLocationPermission();
            }
        });

        stopButton.setOnClickListener(v -> stopLocationSharing());
    }

    private void startLocationSharing() {
        isSharing = true;
        saveBusSharingState(true);
        updateButtonState(true);

        Intent serviceIntent = new Intent(this, LocationService.class);
        serviceIntent.putExtra("BUS_NUMBER", busNumber);
        startService(serviceIntent);
    }

    private void stopLocationSharing() {
        isSharing = false;
        saveBusSharingState(false);
        updateButtonState(false);

        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
        locationTextView.setText(""); // Clear displayed location
    }

    private void updateButtonState(boolean isSharing) {
        sharingSwitch.setChecked(isSharing);
        startButton.setEnabled(!isSharing);
        stopButton.setEnabled(isSharing);
        startButton.setBackgroundColor(isSharing ?
                ContextCompat.getColor(this, R.color.colorButtonDisabled) :
                ContextCompat.getColor(this, R.color.colorButtonEnabled));
    }

    private void saveBusSharingState(boolean isSharing) {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("BUS_" + busNumber + "_SHARING", isSharing);
        editor.apply();
    }

    private void updateUIFromPreferences() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        boolean isSharing = prefs.getBoolean("BUS_" + busNumber + "_SHARING", false);
        updateButtonState(isSharing);
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationSharing();
            } else {
                Toast.makeText(this, "Location permission is required to share location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
