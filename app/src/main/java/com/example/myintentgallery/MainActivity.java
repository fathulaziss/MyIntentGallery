package com.example.myintentgallery;

import static com.example.myintentgallery.CameraActivity.CAMERAX_RESULT;
import static com.example.myintentgallery.Utils.getImageUri;
import static com.example.myintentgallery.Utils.reduceFileImage;
import static com.example.myintentgallery.Utils.uriToFile;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myintentgallery.data.api.ApiConfig;
import com.example.myintentgallery.data.api.ApiService;
import com.example.myintentgallery.data.api.FileUploadResponse;
import com.example.myintentgallery.databinding.ActivityMainBinding;
import android.Manifest;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        if (currentImageUri != null) {
            try {
                // Convert the URI to a File
                File imageFile = reduceFileImage(uriToFile(currentImageUri, this));
                Log.d("Image File", "showImage: " + imageFile.getPath());

                // Description for the image
                String description = "Ini adalah deskripsi gambar";

                // Show loading (assuming you have a method to show loading indicator)
                showLoading(true);

                // Create RequestBody for the description
                RequestBody requestBody = RequestBody.create(description, MediaType.parse("text/plain"));

                // Create RequestBody for the image file
                RequestBody requestImageFile = RequestBody.create(imageFile, MediaType.parse("image/jpeg"));

                // Create MultipartBody.Part for the image file
                MultipartBody.Part multipartBody = MultipartBody.Part.createFormData(
                        "photo",
                        imageFile.getName(),
                        requestImageFile
                );

                // Launch the API request using a background thread (simulating coroutine with AsyncTask, Executor, or other threading)
                ApiService apiService = ApiConfig.getApiService();
                Call<FileUploadResponse> call = apiService.uploadImage(multipartBody, requestBody);

                call.enqueue(new Callback<FileUploadResponse>() {
                    @Override
                    public void onResponse(Call<FileUploadResponse> call, Response<FileUploadResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            FileUploadResponse fileUploadResponse = response.body();
                            showToast(fileUploadResponse.getMessage());
                        } else {
                            showToast(response.message());
                        }
                        showLoading(false);
                    }

                    @Override
                    public void onFailure(Call<FileUploadResponse> call, Throwable t) {
                        showToast("Upload failed. Please try again.");
                        showLoading(false);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                showToast(getString(R.string.empty_image_warning)); // Show error if uriToFile fails
            }
        } else {
            // If currentImageUri is null, show a toast
            showToast(getString(R.string.empty_image_warning));
        }
    }

    // Function to show/hide loading indicator
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressIndicator.setVisibility(View.VISIBLE);
        } else {
            binding.progressIndicator.setVisibility(View.GONE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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