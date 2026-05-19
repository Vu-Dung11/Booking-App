package com.example.bookingapp.presentation.features.profile;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.data.remote.RetrofitClient;
import com.example.bookingapp.data.repository.AuthRepository;

public class ProfileViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public ProfileViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        AuthRepository repo = new AuthRepository(RetrofitClient.getApiService(context));
        return (T) new ProfileViewModel(repo);
    }
}
