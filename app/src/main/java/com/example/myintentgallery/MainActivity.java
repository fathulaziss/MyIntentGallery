package com.example.myintentgallery;

import static com.example.myintentgallery.CameraActivity.CAMERAX_RESULT;
import static com.example.myintentgallery.Utils.getImageUri;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myintentgallery.databinding.ActivityMainBinding;
import android.Manifest;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Uri currentImageUri = null;

    // Declare the ActivityResultLauncher for the gallery
    private ActivityResultLauncher<PickVisualMediaRequest> launcherGallery;

    // Declare the ActivityResultLauncher for camera
    private ActivityResultLauncher<Uri> launcherIntentCamera;

    public static final String REQUIRED_PERMISSION = Manifest.permission.CAMERA;

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(MainActivity.this, "Permission request granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission request denied", Toast.LENGTH_LONG).show();
                }
            });

    private ActivityResultLauncher<Intent> launcherIntentCameraX =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == CAMERAX_RESULT) {
                                Intent data = result.getData();
                                if (data != null) {
                                    String imageUriString = data.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE);
                                    if (imageUriString != null) {
                                        currentImageUri = Uri.parse(imageUriString);
                                        showImage();
                                    }
                                }
                            }
                        }
                    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION);
        }

        // Initialize the launcherGallery ActivityResultLauncher
        launcherGallery = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        currentImageUri = uri;
                        showImage();
                    } else {
                        Log.d("Photo Picker", "No media selected");
                    }
                }
        );

        // Initialize the launcher for taking a picture with the camera
        launcherIntentCamera = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                isSuccess -> {
                    if (isSuccess) {
                        showImage();
                    } else {
                        currentImageUri = null;
                    }
                }
        );

        // Set onClickListeners for each button
        binding.galleryButton.setOnClickListener(v -> startGallery());

        binding.cameraButton.setOnClickListener(v -> startCamera());

        binding.cameraXButton.setOnClickListener(v -> startCameraX());

        binding.uploadButton.setOnClickListener(v -> uploadImage());
    }

    private void startGallery() {
        // Launch the gallery picker for images only
        launcherGallery.launch(new PickVisualMediaRequest());
    }

    // Method for the camera button
    private void startCamera() {
        currentImageUri = getImageUri(this);
        if (currentImageUri != null) {
            launcherIntentCamera.launch(currentImageUri);
        }
    }

    // Method for the CameraX button
    private void startCameraX() {
        Intent intent = new Intent(this, CameraActivity.class);
        launcherIntentCameraX.launch(intent);
    }

    // Method for the upload button
    private void uploadImage() {
        Toast.makeText(this, "Fitur ini belum tersedia", Toast.LENGTH_SHORT).show();
    }

    private void showImage() {
        if (currentImageUri != null) {
            // Log the URI of the selected image
            Log.d("Image URI", "showImage: " + currentImageUri);
            // Set the image URI in the ImageView
            binding.previewImageView.setImageURI(currentImageUri);
        }
    }
}