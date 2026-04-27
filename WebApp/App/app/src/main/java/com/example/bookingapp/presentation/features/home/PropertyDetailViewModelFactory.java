package com.example.bookingapp.presentation.features.home;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.data.remote.RetrofitClient;
import com.example.bookingapp.data.repository.PropertyRepository;

public class PropertyDetailViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public PropertyDetailViewModelFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        PropertyRepository repository = new PropertyRepository(RetrofitClient.getApiService(context));
        return modelClass.cast(new PropertyDetailViewModel(repository));
    }
}

