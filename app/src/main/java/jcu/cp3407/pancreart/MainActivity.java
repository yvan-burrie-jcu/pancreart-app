package jcu.cp3407.pancreart;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 0;
    private final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 0;
    private int notificationID = 0;
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder glucoseLow, glucoseHigh, glucoseNormal, podBatteryLow,
            sensorFailure, insulinReservoirLow, deliveryFailure, insulinLow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationManager = NotificationManagerCompat.from(this);
        createNotifications();

        // Testing for enabling permissions
        enableReadStoragePermission();
        enableWriteStoragePermission();

//        // Testing dashboard layout
//        Intent intent = new Intent(this, DashboardActivity.class);
//        startActivity(intent);

        // Test notifications with intent to DashboardActivity
        notificationManager.notify(notificationID++, glucoseLow.build());

    }

    private void createNotifications() {
        Intent dashboardIntent = new Intent(this, DashboardActivity.class);
        dashboardIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                dashboardIntent, 0);
        // Create additional intents to go to different activity and pass through to desired notification.

        glucoseLow = new Notifications().buildHighPriority(this, pendingIntent,
                getString(R.string.title_glucose_low), getString(R.string.context_text_glucose_low)
        );
        glucoseHigh = new Notifications().buildHighPriority(this, pendingIntent,
                getString(R.string.title_glucose_high), getString(R.string.context_text_glucose_high)
        );
        glucoseNormal = new Notifications().buildHighPriority(this, pendingIntent,
                getString(R.string.title_glucose_normal), getString(R.string.context_text_glucose_normal)
        );
        podBatteryLow = new Notifications().buildHighPriority(this, pendingIntent,
                getString(R.string.title_pod_battery_low), getString(R.string.context_text_pod_battery_low)
        );
        sensorFailure = new Notifications().buildHighPriority(this, pendingIntent,
                getString(R.string.title_sensor_failure), getString(R.string.context_text_sensor_failure)
        );
        insulinReservoirLow = new Notifications().buildHighPriority(this, pendingIntent,
                getString(R.string.title_insulin_reservoir_low), getString(R.string.context_text_insulin_reservoir_low)
        );
        deliveryFailure = new Notifications().buildHighPriority(this, pendingIntent,
                getString(R.string.title_delivery_failure), getString(R.string.context_text_delivery_failure)
        );
        insulinLow = new Notifications().buildHighPriority(this, pendingIntent,
                getString(R.string.title_insulin_low), getString(R.string.context_text_insulin_low)
        );
    }

    private void enableReadStoragePermission() {
        // Check if permission is in current activity.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
        } else {
            Toast.makeText(this, "Permission is already granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableWriteStoragePermission() {
        // Check if permission is in current activity.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        } else {
            Toast.makeText(this, "Permission is already granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void createGlucoseLowNotification() {

    }
}