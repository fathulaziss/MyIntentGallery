package com.example.myintentgallery;

import static com.example.myintentgallery.Utils.createCustomTempFile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.example.myintentgallery.databinding.ActivityCameraBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity {

    public static final String TAG = "CameraActivity";
    private ActivityCameraBinding binding;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
    public static final String EXTRA_CAMERAX_IMAGE = "CameraX Image";
    public static final int CAMERAX_RESULT = 200;
    private ImageCapture imageCapture;
    private OrientationEventListener orientationEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using ViewBinding
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize OrientationEventListener to detect orientation changes
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return;
                }

                int rotation;
                if (orientation >= 45 && orientation < 135) {
                    rotation = Surface.ROTATION_270;
                } else if (orientation >= 135 && orientation < 225) {
                    rotation = Surface.ROTATION_180;
                } else if (orientation >= 225 && orientation < 315) {
                    rotation = Surface.ROTATION_90;
                } else {
                    rotation = Surface.ROTATION_0;
                }

                if (imageCapture != null) {
                    imageCapture.setTargetRotation(rotation);
                }
            }
        };

        // Set onClickListeners for the buttons
        binding.switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraSelector.equals(CameraSelector.DEFAULT_BACK_CAMERA)) {
                    cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                } else {
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                }

                startCamera();
            }
        });
        binding.captureImage.setOnClickListener(v -> takePhoto());
    }

    @Override
    protected void onStart() {
        super.onStart();
        orientationEventListener.enable();
    }

    @Override
    protected void onStop() {
        super.onStop();
        orientationEventListener.disable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        startCamera();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    Preview preview = new Preview.Builder()
                            .build();

                    imageCapture = new ImageCapture.Builder().build();


                    preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

                    // Unbind all use cases before rebinding
                    cameraProvider.unbindAll();

                    // Bind use cases to the camera
                    cameraProvider.bindToLifecycle(
                            CameraActivity.this,
                            cameraSelector,
                            preview,
                            imageCapture
                    );
                } catch (Exception exc) {
                    Toast.makeText(CameraActivity.this, "Gagal memunculkan kamera.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "startCamera: " + exc.getMessage());
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        ImageCapture imageCapture = this.imageCapture;
        if (imageCapture == null) return;

        File photoFile = null;

        try {
            photoFile = createCustomTempFile(getApplicationContext());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_CAMERAX_IMAGE, outputFileResults.getSavedUri().toString());
                        setResult(CAMERAX_RESULT, intent);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(CameraActivity.this, "Gagal mengambil gambar.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onError: " + exception.getMessage());
                    }
                });
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