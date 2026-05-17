package com.example.bookingapp.presentation.features.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.views.PropertySearchResponse;
import com.example.bookingapp.data.repository.PropertyRepository;

import java.util.List;

public class SearchViewModel extends ViewModel {
    private final PropertyRepository repository;
    private final MutableLiveData<Resource<List<PropertySearchResponse>>> resultsState = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<String>>> citiesState = new MutableLiveData<>();

    private String currentCity;
    private String currentCheckIn;
    private String currentCheckOut;
    private Integer currentGuests;

    public SearchViewModel(PropertyRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<PropertySearchResponse>>> getResultsState() { return resultsState; }
    public LiveData<Resource<List<String>>> getCitiesState() { return citiesState; }

    public void search(String city, String checkIn, String checkOut, Integer guests) {
        this.currentCity = city;
        this.currentCheckIn = checkIn;
        this.currentCheckOut = checkOut;
        this.currentGuests = guests;
        repository.searchProperties(city, checkIn, checkOut, guests, resultsState);
    }

    public void refresh() {
        if (currentCity != null) {
            search(currentCity, currentCheckIn, currentCheckOut, currentGuests);
        }
    }

    public void loadCities() {
        repository.getCities(citiesState);
    }
}
