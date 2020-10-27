package jcu.cp3407.pancreart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.util.ArrayList;

public class GraphActivity extends AppCompatActivity {
Button minuteButton;
Button tenMinuteButton;
Button hourButton;
ImageButton backButton;


ArrayList<Fragment> fragments;
GraphFragment1 graphFragment1;
GraphFragment2 graphFragment2;
GraphFragment3 graphFragment3;
FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        minuteButton = findViewById(R.id.minuteButton);
        tenMinuteButton = findViewById(R.id.tenMinuteButton);
        hourButton = findViewById(R.id.hourButton);

        backButton = findViewById(R.id.backButton);

        fragments.add(graphFragment1);
        fragments.add(graphFragment2);
        fragments.add(graphFragment3);

        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, fragments.get(1));

        minuteButton.setOnClickListener((view) -> {
            fragmentTransaction.replace(R.id.fragmentContainer, fragments.get(0));
        });

        tenMinuteButton.setOnClickListener((view) -> {
            fragmentTransaction.replace(R.id.fragmentContainer, fragments.get(1));
        });

        hourButton.setOnClickListener((view) -> {
            fragmentTransaction.replace(R.id.fragmentContainer, fragments.get(2));
        });

        backButton.setOnClickListener((view -> {
            Intent intent = new Intent(this,DashboardActivity.class);
            startActivity(intent);
        }));
        
    }

}