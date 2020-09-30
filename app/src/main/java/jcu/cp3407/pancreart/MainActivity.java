package jcu.cp3407.pancreart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Testing Purposes
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);

    }
}