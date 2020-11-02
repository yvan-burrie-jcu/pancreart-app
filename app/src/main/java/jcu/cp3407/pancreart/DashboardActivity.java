package jcu.cp3407.pancreart;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;

import jcu.cp3407.pancreart.data.LoginDataSource;
import jcu.cp3407.pancreart.data.LoginRepository;
import jcu.cp3407.pancreart.data.model.LoggedInUser;
import jcu.cp3407.pancreart.model.Event;
import jcu.cp3407.pancreart.model.PodHandler;
import jcu.cp3407.pancreart.ui.login.LoginActivity;

public class DashboardActivity extends AppCompatActivity {

    Context context;

    enum Intents {
        REQUEST_ENABLE_BT,
        LOGIN_PAGE,
    }

    public static LoggedInUser user;

    private Toolbar toolbar;
    private Menu menu;

    private ProgressBar glucoseProgressBar;
    private ProgressBar insulinProgressBar;
    private ProgressBar batteryProgressBar;

    private TextView glucoseTextView;
    private TextView insulinTextView;
    private TextView batteryTextView;

    Bluetooth bluetooth;

    Storage storage;

    private final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 0;
    private final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 0;
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder glucoseLow, glucoseHigh, glucoseNormal, podBatteryLow,
            sensorFailure, insulinReservoirLow, deliveryFailure, insulinLow;

    enum NotificationId {
        BATTERY_LOW,
        GLUCOSE_AMOUNT,
        INSULIN_RESERVOIR,
    }

    int lastBatteryPercent;
    int lastGlucoseAmount;
    int lastInsulinPercent;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);

        if (user != null && bundle != null) {
            bundle.putString("user_name", user.getName());
            bundle.putLong("user_id", user.getId());
            bundle.putString("user_email", user.getEmail());
            bundle.putString("user_token", user.getToken());
        }
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (bundle != null) {
            String userName = bundle.getString("user_name");
            long userId = bundle.getLong("user_id");
            String userEmail = bundle.getString("user_email");
            String userToken = bundle.getString("user_token");
        }

        setContentView(R.layout.activity_dashboard);

        context = getApplicationContext();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupViews();
        setProgressBars();

//        setupBluetooth(); // todo: forget about BT for now

        storage = new Storage(context);

        notificationManager = NotificationManagerCompat.from(this);
        setupNotifications();

        enableReadStoragePermission();
        enableWriteStoragePermission();

        setupRegulator();

        batteryProgressBar.setMax(100);
        glucoseProgressBar.setMax(300);
        insulinProgressBar.setMax(100);
    }

    static List<Event> events = new ArrayList<>();

    private void setupRegulator() {

        // Instantiate a timer that is used to control the threads
        Timer timer = new Timer();

        // Specify the speed to run threads
        double speed = 1d / 1000;

        // Setup simulator with various settings
        Simulator simulator = new Simulator(timer, speed);
        simulator.batteryPowerTask.charging = false;
        simulator.batteryPowerTask.percent = 100;
        simulator.glucoseDetectorTask.amount = 100;
        simulator.insulinInjectorTask.reservoirAmount = 2000;
        simulator.currentTime = 4 * 60 * 60;

        // Setup a 24-hour daily routine
        simulator.routines.add(new Simulator.Sleep(
                "Morning Sleep",
                0,
                6)
        );
        simulator.routines.add(new Simulator.Meal(
                "Breakfast",
                7,
                1)
        );
        simulator.routines.add(new Simulator.Exercise(
                "Ride to Work",
                8,
                1)
        );
        simulator.routines.add(new Simulator.Stress(
                "Morning Stress",
                10,
                1)
        );
        simulator.routines.add(new Simulator.Meal(
                "Lunch",
                12,
                1)
        );
        simulator.routines.add(new Simulator.Stress(
                "Afternoon Stress",
                13,
                2)
        );
        simulator.routines.add(new Simulator.Exercise(
                "Ride back Home",
                16,
                1)
        );
        simulator.routines.add(new Simulator.Meal(
                "Dinner",
                18,
                2)
        );
        simulator.routines.add(new Simulator.Sleep(
                "Night Sleep",
                21,
                3)
        );

        Regulator regulator = new Regulator(timer, speed);

        GregorianCalendar calendar = new GregorianCalendar();
        long startTime = calendar.getTime().getTime();

//        MiniPID controller = new MiniPID(0.25, 0.01, 0.4);

        simulator.insulinInjectorTask.refill(2000);

        // Set out UI thread
        new Task(timer, 1, 1) {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    lastBatteryPercent = (int) simulator.batteryPowerTask.percent;
                    updateBatteryProgressBar((int) lastBatteryPercent);
                    updateGlucoseProgressBar((int) lastGlucoseAmount);
                    lastInsulinPercent = 100 / 2000 * (int) simulator.insulinInjectorTask.reservoirAmount;
                    updateInsulinProgressBar((int) lastInsulinPercent);
                });
            }
        }.initiate();

        // Setup all the callbacks
        PodHandler handler = new PodHandler() {

            @Override
            public void onGlucoseDetected(double amount) {
                super.onGlucoseDetected(amount);
                events.add(new Event(
                        0,
                        1,
                        Event.Type.GLUCOSE_READING,
                        startTime + simulator.currentTime + (simulator.days * 24 * 60 * 60),
                        amount)
                );
                lastGlucoseAmount = (int) amount;
                if (amount > 160) {
                    notificationManager.notify(NotificationId.GLUCOSE_AMOUNT.ordinal(), glucoseHigh.build());
                } else if (amount < 60) {
                    notificationManager.notify(NotificationId.GLUCOSE_AMOUNT.ordinal(), glucoseLow.build());
                }

                if (simulator.batteryPowerTask.percent > 90) {
                    simulator.batteryPowerTask.charging = false;
                }
            }

            @Override
            public void onInsulinInjected(double amount) {
                super.onInsulinInjected(amount);
                events.add(new Event(
                        0,
                        1,
                        Event.Type.INSULIN_INJECTION,
                        calendar.getTime().getTime() + simulator.currentTime,
                        amount)
                );
            }

            @Override
            public void onBatteryPowerLow(double percent) {
                super.onBatteryPowerLow(percent);
                simulator.batteryPowerTask.charging = true;
                notificationManager.notify(NotificationId.BATTERY_LOW.ordinal(), podBatteryLow.build());
                lastBatteryPercent = (int) percent;
            }

            @Override
            public void onInsulinReservoirLow() {
                super.onInsulinReservoirLow();
                simulator.insulinInjectorTask.refill(2000);
                notificationManager.notify(NotificationId.INSULIN_RESERVOIR.ordinal(), insulinReservoirLow.build());
            }

            @Override
            public void onRegulateDosage() {
                if (simulator.batteryPowerTask.percent <= 0) {
                    return;
                }
                super.onRegulateDosage();
                if (lastGlucoseAmount > 150) {
                    simulator.insulinInjectorTask.dose(lastGlucoseAmount / 10);
                }
            }
        };
        simulator.assignHandler(handler);
        regulator.assignHandler(handler);

        // Threads can now start
        simulator.initiate();
        regulator.initiate();
    }

    private void updateBatteryProgressBar(int percent) {
        batteryProgressBar.setProgress((int) percent);
        batteryTextView.setText(String.valueOf(percent));
    }

    private void updateGlucoseProgressBar(int amount) {
        glucoseProgressBar.setProgress(amount);
        glucoseTextView.setText(String.valueOf(amount));
    }

    private void updateInsulinProgressBar(int amount) {
        insulinProgressBar.setProgress(amount);
        insulinTextView.setText(String.valueOf(amount));
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
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_login: {
                if (user == null) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivityForResult(intent, Intents.LOGIN_PAGE.ordinal());
                } else {
                    LoginRepository.getInstance(new LoginDataSource()).logout(
                            getResources().getString(R.string.home_address));
                }
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
        if (requestCode == Intents.LOGIN_PAGE.ordinal()) {
            updateLoginButton();
        }
    }

    private void updateLoginButton() {
        MenuItem item = menu.findItem(R.id.menu_item_login);
        if (user != null) {
            item.setTitle(R.string.logout);
        } else {
            item.setTitle(R.string.login);
        }
    }

    private void setProgressBars() {
        final int glucoseLowThreshold = 3;
        final int glucoseHighThreshold = 7;
        final int percentageMiddleThreshold = 50;
        final int percentageLowThreshold = 25;
        final Drawable breachedThresholdColour = ResourcesCompat.getDrawable(
                getResources(),
                R.drawable.circle_breached_threshold_foreground,
                null);
        final Drawable middleThresholdColour = ResourcesCompat.getDrawable(
                getResources(),
                R.drawable.circle_middle_threshold_foreground,
                null);

//        // Determine which colour to set.
//        if (glucoseLevel >= glucoseHighThreshold || glucoseLevel <= glucoseLowThreshold) {
//            // Set colour to breached threshold.
//            glucoseProgressBar.setProgressDrawable(breachedThresholdColour);
//        }
//
//        if (insulinLevel >= percentageLowThreshold && insulinLevel < percentageMiddleThreshold) {
//            // Set colour to middle threshold.
//            insulinProgressBar.setProgressDrawable(middleThresholdColour);
//        } else if (insulinLevel < percentageLowThreshold) {
//            // Set colour to breached threshold.
//            insulinProgressBar.setProgressDrawable(breachedThresholdColour);
//        }
//
//        if (batteryLevel >= percentageLowThreshold && batteryLevel < percentageMiddleThreshold) {
//            // Set colour to middle threshold.
//            batteryProgressBar.setProgressDrawable(middleThresholdColour);
//        } else if (batteryLevel < percentageLowThreshold) {
//            // Set colour to breached threshold.
//            batteryProgressBar.setProgressDrawable(breachedThresholdColour);
//        }
    }

    private void setupViews() {
        glucoseProgressBar = findViewById(R.id.glucose_circle_progress_bar);
        insulinProgressBar = findViewById(R.id.insulin_circle_progress_bar);
        batteryProgressBar = findViewById(R.id.battery_circle_progress_bar);
        glucoseTextView = findViewById(R.id.glucose_text);
        insulinTextView = findViewById(R.id.insulin_text);
        batteryTextView = findViewById(R.id.battery_text);
    }

    PendingIntent pendingIntent;

    private void setupNotifications() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        glucoseLow = new Notifications().buildHighPriority(
                DashboardActivity.this,
                pendingIntent,
                getString(R.string.title_glucose_low),
                getString(R.string.context_text_glucose_low)
        );
        glucoseHigh = new Notifications().buildHighPriority(
                DashboardActivity.this,
                pendingIntent,
                getString(R.string.title_glucose_high),
                getString(R.string.context_text_glucose_high)
        );
        podBatteryLow = new Notifications().buildHighPriority(
                DashboardActivity.this,
                pendingIntent,
                getString(R.string.title_pod_battery_low),
                getString(R.string.context_text_pod_battery_low)
        );
        insulinReservoirLow = new Notifications().buildHighPriority(
                DashboardActivity.this,
                pendingIntent,
                getString(R.string.title_insulin_reservoir_low),
                getString(R.string.context_text_insulin_reservoir_low)
        );
    }

    private void enableReadStoragePermission() {
        // Check if permission is in current activity.
        int permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request permission.
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
        }
    }

    private void enableWriteStoragePermission() {
        // Check if permission is in current activity.
        int permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request permission.
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        }
    }
}
