package com.example.bookingapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.ApiResponse;
import com.example.bookingapp.data.model.auth.AuthResponse;
import com.example.bookingapp.data.model.auth.LoginRequest;
import com.example.bookingapp.data.model.auth.RegisterRequest;
import com.example.bookingapp.data.remote.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private final ApiService apiService;

    public AuthRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public void login(LoginRequest request, MutableLiveData<Resource<AuthResponse>> loginState) {
        loginState.setValue(Resource.loading());

        apiService.login(request).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> body = response.body();
                    if (body.getData() != null) {
                        loginState.setValue(Resource.success(body.getData()));
                    } else {
                        loginState.setValue(Resource.error(body.getMessage() != null ? body.getMessage() : "Lỗi từ Server", null));
                    }
                } else {
                    loginState.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                loginState.setValue(Resource.error(t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "Lỗi mạng", null));
            }
        });
    }

    public void register(RegisterRequest registerRequest, MutableLiveData<Resource<AuthResponse>> registerState){
        registerState.setValue(Resource.loading());
        apiService.register(registerRequest).enqueue(new Callback<ApiResponse<AuthResponse>>(){


            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> body = response.body();
                    if (body.getData() != null) {
                        registerState.setValue(Resource.success(body.getData()));
                    } else {
                        registerState.setValue(Resource.error(body.getMessage() != null ? body.getMessage() : "Lỗi từ Server", null));
                    }
                } else {
                    registerState.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                registerState.setValue(Resource.error(t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "Lỗi mạng", null));
            }
        });
    }



}
