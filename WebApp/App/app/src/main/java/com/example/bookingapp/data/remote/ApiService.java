package com.example.bookingapp.data.remote;

import com.example.bookingapp.data.model.ApiResponse;
import com.example.bookingapp.data.model.PageResponse;
import com.example.bookingapp.data.model.auth.AuthResponse;
import com.example.bookingapp.data.model.auth.LoginRequest;
import com.example.bookingapp.data.model.auth.RegisterRequest;
import com.example.bookingapp.data.model.views.PropertyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/v1/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @POST("api/v1/auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest request);

    @GET("api/v1/properties")
    Call<ApiResponse<PageResponse<PropertyResponse>>> getAllProperties(
            @Query("page") int page,
            @Query("size") int size
    );



    @GET("api/v1/properties/{id}")
    Call<ApiResponse<Object>> getPropertyById(@Path("id") Long id);
}
