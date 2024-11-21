package com.example.myintentgallery;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myintentgallery.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set onClickListeners for each button
        binding.galleryButton.setOnClickListener(v -> startGallery());

        binding.cameraButton.setOnClickListener(v -> startCamera());

        binding.cameraXButton.setOnClickListener(v -> startCameraX());

        binding.uploadButton.setOnClickListener(v -> uploadImage());
    }

    // Method for the gallery button
    private void startGallery() {
        Toast.makeText(this, "Fitur ini belum tersedia", Toast.LENGTH_SHORT).show();
    }

    // Method for the camera button
    private void startCamera() {
        Toast.makeText(this, "Fitur ini belum tersedia", Toast.LENGTH_SHORT).show();
    }

    // Method for the CameraX button
    private void startCameraX() {
        Toast.makeText(this, "Fitur ini belum tersedia", Toast.LENGTH_SHORT).show();
    }

    // Method for the upload button
    private void uploadImage() {
        Toast.makeText(this, "Fitur ini belum tersedia", Toast.LENGTH_SHORT).show();
    }
}