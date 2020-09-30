package jcu.cp3407.pancreart;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class DashboardActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        findViewByIds();
        setProgressBars();


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
    }
}