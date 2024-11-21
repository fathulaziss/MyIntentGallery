package com.example.myintentgallery;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myintentgallery.databinding.ActivityCameraBinding;

public class CameraActivity extends AppCompatActivity {

    private ActivityCameraBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using ViewBinding
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set onClickListeners for the buttons
        binding.switchCamera.setOnClickListener(v -> startCamera());
        binding.captureImage.setOnClickListener(v -> takePhoto());
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        startCamera();
    }

    private void startCamera() {
        // showCamera logic here
    }

    private void takePhoto() {
        // takePhoto logic here
    }

    private void hideSystemUI() {
        // Hide system UI to make the activity full screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            getWindow().getInsetsController().hide(WindowInsets.Type.statusBars());
        } else {
            // For below Android 11
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
}