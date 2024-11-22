package com.example.myintentgallery.data.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiConfig {

    private static final String BASE_URL = "https://story-api.dicoding.dev/v1/";

    public static ApiService getApiService() {
        // Create the logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Build the OkHttpClient and add the logging interceptor
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        // Create Retrofit instance with the configured OkHttpClient and Gson converter
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        // Return the ApiService instance
        return retrofit.create(ApiService.class);
    }
}
