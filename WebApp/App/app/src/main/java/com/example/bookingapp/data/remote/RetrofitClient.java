package com.example.bookingapp.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Chạy trên máy thật: thay bằng IP thật của máy tính (vd: http://192.168.1.5:8080/)
    // Chạy trên emulator: dùng 10.0.2.2 thay cho localhost
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    private static Retrofit instance;

    public static Retrofit getInstance() {
        if (instance == null) {
            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }

    public static ApiService getApiService() {
        return getInstance().create(ApiService.class);
    }
}
