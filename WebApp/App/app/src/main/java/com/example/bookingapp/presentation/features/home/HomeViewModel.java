package com.example.bookingapp.presentation.features.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.PageResponse;
import com.example.bookingapp.data.model.views.PropertyResponse;
import com.example.bookingapp.data.repository.PropertyRepository;

public class HomeViewModel extends ViewModel {
    private final PropertyRepository repository;
    private final MutableLiveData<Resource<PageResponse<PropertyResponse>>> propertiesState = new MutableLiveData<>();

    public HomeViewModel(PropertyRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<PageResponse<PropertyResponse>>> getPropertiesState() {
        return propertiesState;
    }

    public void loadProperties() {
        repository.getAllProperties(0, 10, propertiesState);
    }
}
