package com.example.bookingapp.presentation.features.auth;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.data.remote.RetrofitClient;
import com.example.bookingapp.data.repository.AuthRepository;

public class AuthViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public AuthViewModelFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        AuthRepository repository = new AuthRepository(RetrofitClient.getApiService(context));
        return (T) new AuthViewModel(repository);
    }
}