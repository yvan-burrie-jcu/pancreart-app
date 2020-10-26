package jcu.cp3407.pancreart;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class GraphActivity extends AppCompatActivity {
Button minuteButton;
Button tenMinuteButton;
Button hourButton;
ImageButton backButton;
FrameLayout fragmentContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        minuteButton = findViewById(R.id.minuteButton);
        tenMinuteButton = findViewById(R.id.tenMinuteButton);
        hourButton = findViewById(R.id.hourButton);

        backButton = findViewById(R.id.backButton);

        fragmentContainer = findViewById(R.id.fragmentContainer);
    }

}