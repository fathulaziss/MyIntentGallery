package com.example.myintentgallery;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    private static final String FILENAME_FORMAT = "yyyyMMdd_HHmmss";
    private static final String timeStamp = new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(new Date());

    // Function to get the image URI
    public static Uri getImageUri(Context context) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp + ".jpg");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MyCamera/");

            uri = context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
            );
        }
        return uri != null ? uri : getImageUriForPreQ(context);
    }

    // Function to handle image URI for devices below Android Q
    private static Uri getImageUriForPreQ(Context context) {
        File filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(filesDir, "MyCamera/" + timeStamp + ".jpg");

        // Ensure the parent directory exists
        if (imageFile.getParentFile() != null && !imageFile.getParentFile().exists()) {
            boolean mkdirSuccess = imageFile.getParentFile().mkdirs();
            Log.d("ImageUtils", "Directory created: " + mkdirSuccess);
        }

        // Use FileProvider to get the URI for the image file
        return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                imageFile
        );
    }
}
