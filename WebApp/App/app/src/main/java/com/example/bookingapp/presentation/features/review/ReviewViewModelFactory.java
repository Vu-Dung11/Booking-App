package com.example.bookingapp.presentation.features.review;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.data.remote.RetrofitClient;
import com.example.bookingapp.data.repository.ReviewRepository;

public class ReviewViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public ReviewViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        ReviewRepository repo = new ReviewRepository(RetrofitClient.getApiService(context));
        if (modelClass.isAssignableFrom(ReviewCreateViewModel.class)) {
            return (T) new ReviewCreateViewModel(repo);
        }
        return (T) new ReviewListViewModel(repo);
    }
}
