package jcu.cp3407.pancreart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

public class GraphActivity extends AppCompatActivity {

    private GraphFragment graphFragment;

    private int currentDay, currentMonth, currentYear;

    private PopupWindow popupWindow;
    private DatePicker datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_graph);

            Button button = findViewById(R.id.current_button);
            button.setOnClickListener((view) -> {
                popupWindow.showAtLocation(graphFragment.getView(), Gravity.CENTER, 0, 0);
            });

            createPopupWindow();

            Toolbar toolbar = findViewById(R.id.graphToolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        } else {
            setContentView(R.layout.activity_graph_large);
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        // Display default graph with current date
        graphFragment = new GraphFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, graphFragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void createPopupWindow() {
        final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final double THREE_QUARTERS = 3d / 4;
        int[] screenDimensions = getScreenDimensions();

        assert inflater != null;
        @SuppressLint("InflateParams")
        View popupView = inflater.inflate(R.layout.popup_date_picker, null);
        popupWindow = new PopupWindow(
                popupView,
                screenDimensions[0] *= THREE_QUARTERS,
                screenDimensions[1] *= THREE_QUARTERS,
                true);

        datePicker = popupView.findViewById(R.id.date_picker);
        popupView.findViewById(R.id.submit_button).setOnClickListener(v -> {
            currentDay = datePicker.getDayOfMonth();
            currentMonth = datePicker.getMonth();
            currentYear = datePicker.getYear();
            popupWindow.dismiss();
            graphFragment.update(currentDay, currentMonth, currentYear);
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // todo: full screen graph
            graphFragment.update(currentDay, currentMonth, currentYear);
        }
    }

    private int[] getScreenDimensions() {
        return new int[]{
                Resources.getSystem().getDisplayMetrics().widthPixels,
                Resources.getSystem().getDisplayMetrics().heightPixels};
    }
}
