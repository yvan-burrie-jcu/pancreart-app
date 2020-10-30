package jcu.cp3407.pancreart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.PopupWindow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class GraphActivity extends AppCompatActivity {
    private GraphFragment graphFragment;

    private int currentDay, currentMonth, currentYear;
    private int previousDay;
    private int nextDay;
    private PopupWindow popupWindow;
    private DatePicker datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        // Display default graph with current date
        displayGraphFragment();
        createPopupWindow();
        setButtonListeners();
    }

    private void createPopupWindow() {
        final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final double THREE_QUARTERS = 0.75;
        int[] screenDimensions = getScreenDimensions();

        assert inflater != null;
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.popup_date_picker, null);
        popupWindow = new PopupWindow(popupView, screenDimensions[0] *= THREE_QUARTERS,
                screenDimensions[1] *= THREE_QUARTERS, true);

        // Create listener for submit button
        datePicker = popupView.findViewById(R.id.date_picker);
        popupView.findViewById(R.id.submit_button).setOnClickListener(v -> {
            // Show previous day data from SQLite database
            currentDay = datePicker.getDayOfMonth();
            currentMonth = datePicker.getMonth();
            currentYear = datePicker.getYear();

            previousDay = currentDay - 1;
            nextDay = currentDay + 1;
            popupWindow.dismiss();
        });
    }

    private void displayGraphFragment() {
        graphFragment = new GraphFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, graphFragment);
        fragmentTransaction.commit();
    }

    private int[] getScreenDimensions() {
        return new int[]{Resources.getSystem().getDisplayMetrics().widthPixels,
                Resources.getSystem().getDisplayMetrics().heightPixels};
    }

    private void setButtonListeners() {
        Button previousButton = findViewById(R.id.previous_button);
        Button currentButton = findViewById(R.id.current_button);
        Button nextButton = findViewById(R.id.next_button);

        previousButton.setOnClickListener((view) -> {
            // Show previous day data from SQLite database
            graphFragment.update(previousDay, currentMonth, currentYear);
        });

        currentButton.setOnClickListener((view) -> {
           // Show date picker widget
            popupWindow.showAtLocation(graphFragment.getView(), Gravity.CENTER, 0, 0);
        });

        nextButton.setOnClickListener((view) -> {
            // Show next day data from SQLite database
            graphFragment.update(nextDay, currentMonth, currentYear);
        });
    }
}