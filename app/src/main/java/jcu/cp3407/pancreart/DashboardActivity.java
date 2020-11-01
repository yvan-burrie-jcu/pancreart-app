package jcu.cp3407.pancreart;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import jcu.cp3407.pancreart.ui.login.LoginActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    Context context;

    enum Intents {
        REQUEST_ENABLE_BT,
        LOGIN_PAGE,
    }

    private Toolbar toolbar;

    private Menu menu;
    private MenuItem loginLogout;

    private ProgressBar glucoseProgressBar;
    private ProgressBar insulinProgressBar;
    private ProgressBar batteryProgressBar;

    private TextView glucoseTextView;
    private TextView insulinTextView;
    private TextView batteryTextView;

    // Testing values
    private int glucoseLevel = 6;
    private int insulinLevel = 45;
    private int batteryLevel = 20;

    Bluetooth bluetooth;

    Storage storage;

    private final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 0;
    private final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 0;
    private int notificationID = 0;
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder glucoseLow, glucoseHigh, glucoseNormal, podBatteryLow,
            sensorFailure, insulinReservoirLow, deliveryFailure, insulinLow;

    // Get Blood Events
    Event[] events; // web server

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        context = getApplicationContext();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewByIds();
        setProgressBars();

//        setupBluetooth(); // todo: forget about BT for now

        storage = new Storage(context);

        notificationManager = NotificationManagerCompat.from(this);
        createNotifications();

        // Testing for enabling permissions
        enableReadStoragePermission();
        enableWriteStoragePermission();

        // Test notifications with intent to DashboardActivity
        notificationManager.notify(notificationID++, glucoseLow.build());
    }

    // Here are all bluetooth events to handle
    @SuppressLint("HandlerLeak")
    private void setupBluetooth() {
        bluetooth = new Bluetooth(new Handler() {
            @Override
            public void handleMessage(Message message) {
                super.handleMessage(message);

                switch (message.what) {
                    case Bluetooth.MESSAGE_READ: {
                        String data = new String((byte[]) message.obj);
                        // todo: handle data received
                        break;
                    }
                    case Bluetooth.MESSAGE_WRITE: {
                        BluetoothSocket socket = (BluetoothSocket) message.obj;
                        String data = "00"; // todo: sample data to send
                        bluetooth.writeWithConnectedThread(socket, data);
                        break;
                    }
                    case Bluetooth.CONNECTED: {
                        Toast.makeText(context, R.string.bt_device_connected, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case Bluetooth.CONNECTING: {
                        Toast.makeText(context, R.string.bt_device_connecting, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case Bluetooth.NO_SOCKET_FOUND: {
                        Toast.makeText(context, R.string.bt_device_not_found, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case Bluetooth.ADAPTER_UNAVAILABLE: {
                        Toast.makeText(context, R.string.bt_device_not_supported, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case Bluetooth.ADAPTER_DISABLED: {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, Intents.REQUEST_ENABLE_BT.ordinal());

                        // todo: consider requesting user to go to settings instead
//                        intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
//                        startActivity(intent);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, this.menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_login: {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, Intents.LOGIN_PAGE.ordinal());
                return true;
            }
            case R.id.menu_item_graph: {
                Intent intent = new Intent(this, GraphActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.menu_item_pod: {
                // todo: start connecting or disconnecting pod
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == resultCode) {
            loginLogout.setTitle(R.string.logout);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setProgressBars() {
        final int glucoseLowThreshold = 3;
        final int glucoseHighThreshold = 7;
        final int percentageMiddleThreshold = 50;
        final int percentageLowThreshold = 25;
        final Drawable breachedThresholdColour = ResourcesCompat.getDrawable(getResources(),
                R.drawable.circle_breached_threshold_foreground, null);
        final Drawable middleThresholdColour = ResourcesCompat.getDrawable(getResources(),
                R.drawable.circle_middle_threshold_foreground, null);

        // Determine which colour to set.
        if (glucoseLevel >= glucoseHighThreshold || glucoseLevel <= glucoseLowThreshold) {
            // Set colour to breached threshold.
            glucoseProgressBar.setProgressDrawable(breachedThresholdColour);
        }

        if (insulinLevel >= percentageLowThreshold && insulinLevel < percentageMiddleThreshold) {
            // Set colour to middle threshold.
            insulinProgressBar.setProgressDrawable(middleThresholdColour);
        } else if (insulinLevel < percentageLowThreshold) {
            // Set colour to breached threshold.
            insulinProgressBar.setProgressDrawable(breachedThresholdColour);
        }

        if (batteryLevel >= percentageLowThreshold && batteryLevel < percentageMiddleThreshold) {
            // Set colour to middle threshold.
            batteryProgressBar.setProgressDrawable(middleThresholdColour);
        } else if (batteryLevel < percentageLowThreshold) {
            // Set colour to breached threshold.
            batteryProgressBar.setProgressDrawable(breachedThresholdColour);
        }

        // Set progress with text.
        glucoseProgressBar.setProgress(glucoseLevel);
        insulinProgressBar.setProgress(insulinLevel);
        batteryProgressBar.setProgress(batteryLevel);
        glucoseTextView.setText(String.valueOf(glucoseLevel));
        insulinTextView.setText(String.valueOf(insulinLevel));
        batteryTextView.setText(String.valueOf(batteryLevel));
    }

    private void findViewByIds() {
        glucoseProgressBar = findViewById(R.id.glucose_circle_progress_bar);
        insulinProgressBar = findViewById(R.id.insulin_circle_progress_bar);
        batteryProgressBar = findViewById(R.id.battery_circle_progress_bar);
        glucoseTextView = findViewById(R.id.glucose_text);
        insulinTextView = findViewById(R.id.insulin_text);
        batteryTextView = findViewById(R.id.battery_text);
        loginLogout = findViewById(R.id.menu_item_login);
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
        }
    }
}
