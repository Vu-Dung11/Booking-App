package com.example.bookingapp.presentation.features.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.auth.AuthResponse;
import com.example.bookingapp.data.model.auth.LoginRequest;
import com.example.bookingapp.data.repository.AuthRepository;

public class AuthViewModel extends ViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<Resource<AuthResponse>> loginState = new MutableLiveData<>();

    public AuthViewModel(AuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<AuthResponse>> getLoginState() {
        return loginState;
    }

    public void login(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);
        repository.login(request, loginState);
    }
}
