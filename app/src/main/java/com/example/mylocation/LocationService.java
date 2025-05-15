package com.example.mylocation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service {

    private static final String CHANNEL_ID = "LocationServiceChannel";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private DatabaseReference busLocationRef;
    private int busNumber;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Location Services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Firebase Database
        busLocationRef = FirebaseDatabase.getInstance().getReference("buses");

        // Create a notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Set up the LocationCallback
        setupLocationCallback();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            busNumber = intent.getIntExtra("BUS_NUMBER", 1);

            // Start location updates
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                stopSelf();
                return START_NOT_STICKY;
            }
            fusedLocationClient.requestLocationUpdates(createLocationRequest(), locationCallback, null);

            // Show notification
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Location Service")
                    .setContentText("Tracking location...")
                    .setSmallIcon(R.drawable.ic_launcher) // replace with your icon
                    .build();

            startForeground(1, notification);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback);

        // Remove the bus location from Firebase when the service is destroyed
        String busLocationKey = "bus" + busNumber + "location";
        busLocationRef.child(busLocationKey).removeValue();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@Nullable LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (android.location.Location location : locationResult.getLocations()) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Log.d("LocationService", "Latitude: " + latitude + ", Longitude: " + longitude);

                        // Save the location data to Firebase under the correct bus ID
                        saveLocationToFirebase(latitude, longitude);
                    }
                }
            }
        };
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void saveLocationToFirebase(double latitude, double longitude) {
        String busLocationKey = "bus" + busNumber + "location";
        // Create a map for the new structure
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);

        // Save the location data to Firebase under the correct bus ID
        busLocationRef.child(busLocationKey).setValue(locationData);
    }
}
