package com.example.tiktokautoscroll;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView statusText = findViewById(R.id.statusText);
        Button openAccessibility = findViewById(R.id.openAccessibility);
        Button openOverlay = findViewById(R.id.openOverlay);

        statusText.setText("1) Enable accessibility service. 2) Grant overlay permission. 3) Open TikTok and use the floating control.");

        openAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        openOverlay.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });
    }
}
