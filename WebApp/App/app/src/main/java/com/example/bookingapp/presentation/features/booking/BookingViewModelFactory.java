package com.example.bookingapp.presentation.features.booking;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.data.remote.RetrofitClient;
import com.example.bookingapp.data.repository.BookingRepository;

public class BookingViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public BookingViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        BookingRepository repo = new BookingRepository(RetrofitClient.getApiService(context));
        if (modelClass.isAssignableFrom(BookingDetailViewModel.class)) {
            return (T) new BookingDetailViewModel(repo);
        }
        return (T) new BookingViewModel(repo);
    }
}
