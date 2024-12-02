package com.example.bepviet02;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bepviet02.databinding.ActivitySplashBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private final int splashScreenTime = 3000; // 3s
    private final int timeInterval = 100; // 0.1s
    private int progress = 0; // 0->100 progress bar
    private Runnable runnable;
    private Handler handler;

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ViewBinding
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Set progress bar
        binding.progressBar.setMax(splashScreenTime); // 3000 / 100 = 30
        binding.progressBar.setProgress(progress); // progressbar start = 0
        handler = new Handler(Looper.getMainLooper()); // create handler for progressbar
        runnable = () -> {
            if (progress < splashScreenTime) {
                progress += timeInterval;
                binding.progressBar.setProgress(progress);
                handler.postDelayed(runnable, timeInterval);
            } else {
                // Check if user have logged in or not
                FirebaseApp.initializeApp(this);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                // If user is null -> redirect to login screen, if not -> redirect to main screen
                startActivity(new Intent(SplashActivity.this,
                        user != null ? MainActivity.class : LoginActivity.class));
                finish();
            }
        };
        handler.postDelayed(runnable, timeInterval); // start handler

    }
}