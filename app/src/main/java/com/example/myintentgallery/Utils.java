package com.example.myintentgallery;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    private static final String FILENAME_FORMAT = "yyyyMMdd_HHmmss";
    private static final String timeStamp = new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(new Date());
    public static final int MAXIMAL_SIZE = 1000000;

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

    public static File createCustomTempFile(Context context) throws IOException {
        File filesDir = context.getExternalCacheDir();
        String timeStamp = String.valueOf(System.currentTimeMillis()); // or use any other timestamp logic
        return File.createTempFile(timeStamp, ".jpg", filesDir);
    }

    public static File uriToFile(Uri imageUri, Context context) throws IOException {
        // Create a temporary file using a method like createCustomTempFile
        File myFile = createCustomTempFile(context);

        // Open input stream from the URI
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);

        // Create output stream to write data to the temporary file
        OutputStream outputStream = new FileOutputStream(myFile);

        byte[] buffer = new byte[1024];
        int length;

        // Read from input stream and write to output stream
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        // Close the streams
        outputStream.close();
        inputStream.close();

        return myFile;
    }

    public static File reduceFileImage(File file) throws IOException {
        // Decode the image file to a Bitmap
        Bitmap bitmap =  BitmapFactory.decodeFile(file.getAbsolutePath());
        bitmap = getRotatedBitmap(bitmap, file);
        int compressQuality = 100;
        int streamLength;

        // Iterate to compress the image until the size is below the MAXIMAL_SIZE
        do {
            ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream);
            byte[] bmpPicByteArray = bmpStream.toByteArray();
            streamLength = bmpPicByteArray.length;
            compressQuality -= 5; // Reduce the quality by 5 each time
        } while (streamLength > MAXIMAL_SIZE);

        // Compress the bitmap with the final quality and write it back to the file
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, out);
        }

        return file; // Return the file with the reduced size
    }

    public static Bitmap getRotatedBitmap(Bitmap bitmap, File file) {
        try {
            // Get the EXIF orientation value from the file
            ExifInterface exif = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                exif = new ExifInterface(file);
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            // Rotate the bitmap based on the EXIF orientation
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(bitmap, 270);
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    return bitmap;  // No rotation required
            }
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;  // Return original bitmap if there's an error reading EXIF data
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        // Create a new Matrix for rotation
        Matrix matrix = new Matrix();

        // Post-rotate the matrix by the given angle
        matrix.postRotate(angle);

        // Create and return a new Bitmap using the matrix
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
