package com.example.bookingapp.presentation.features.favorite;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.data.local.AppDatabase;
import com.example.bookingapp.data.repository.FavoriteRepository;

public class FavoriteViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public FavoriteViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        FavoriteRepository repo = new FavoriteRepository(AppDatabase.get(context).favoriteDao());
        return (T) new FavoriteViewModel(repo);
    }
}
