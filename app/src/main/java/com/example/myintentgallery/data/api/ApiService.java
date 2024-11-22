package com.example.myintentgallery.data.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @Multipart
    @POST("stories/guest")
    Call<FileUploadResponse> uploadImage(
            @Part MultipartBody.Part file,
            @Part("description") RequestBody description
    );
}
