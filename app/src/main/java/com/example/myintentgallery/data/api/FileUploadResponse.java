package com.example.myintentgallery.data.api;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class FileUploadResponse {

    @SerializedName("error")
    private boolean error;

    @SerializedName("message")
    private String message;

    // Constructor
    public FileUploadResponse(boolean error, String message) {
        this.error = error;
        this.message = message;
    }

    // Getters and Setters
    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @NonNull
    @Override
    public String toString() {
        return "FileUploadResponse{" +
                "error=" + error +
                ", message='" + message + '\'' +
                '}';
    }
}
