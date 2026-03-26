package com.example.bookingapp.presentation.features.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.data.remote.RetrofitClient;
import com.example.bookingapp.data.repository.AuthRepository;

public class AuthViewModelFactory implements ViewModelProvider.Factory{

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            AuthRepository repository = new AuthRepository(RetrofitClient.getApiService());
            return (T) new AuthViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
    }
}
