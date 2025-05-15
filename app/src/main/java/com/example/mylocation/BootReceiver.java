package com.example.mylocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // Start the LocationService when the phone boots
            Intent serviceIntent = new Intent(context, LocationService.class);
            context.startForegroundService(serviceIntent);
        }
    }
}
