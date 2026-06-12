package com.example.bookingapp.presentation.features.favorite;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookingapp.data.local.FavoriteProperty;
import com.example.bookingapp.data.repository.FavoriteRepository;

import java.util.List;

public class FavoriteViewModel extends ViewModel {
    private final FavoriteRepository repository;

    public FavoriteViewModel(FavoriteRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<FavoriteProperty>> getFavorites() { return repository.observeAll(); }
    public LiveData<List<Long>> getFavoriteIds() { return repository.observeAllIds(); }

    public void toggle(long propertyId, String name, String thumbnailUrl, String city,
                       Double minPrice, Double averageRating,
                       FavoriteRepository.OnToggleResult cb) {
        FavoriteProperty fav = new FavoriteProperty(
                propertyId,
                name != null ? name : "",
                thumbnailUrl,
                city,
                minPrice,
                averageRating,
                System.currentTimeMillis()
        );
        repository.toggle(fav, cb);
    }
}
