package com.example.bookingapp.presentation.features.booking;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.data.remote.RetrofitClient;
import com.example.bookingapp.data.repository.BookingRepository;
import com.example.bookingapp.data.repository.PropertyRepository;

public class BookingCreateViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public BookingCreateViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new BookingCreateViewModel(
                new PropertyRepository(RetrofitClient.getApiService(context)),
                new BookingRepository(RetrofitClient.getApiService(context))
        );
    }
}
