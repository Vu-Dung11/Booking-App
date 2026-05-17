package com.example.bookingapp.presentation.features.search;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.data.remote.RetrofitClient;
import com.example.bookingapp.data.repository.PropertyRepository;

public class SearchViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public SearchViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        PropertyRepository repository = new PropertyRepository(RetrofitClient.getApiService(context));
        return (T) new SearchViewModel(repository);
    }
}
