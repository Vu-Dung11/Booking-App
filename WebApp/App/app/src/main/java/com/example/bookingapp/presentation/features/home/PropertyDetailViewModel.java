package com.example.bookingapp.presentation.features.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.views.PropertyDetailResponse;
import com.example.bookingapp.data.repository.PropertyRepository;

import androidx.lifecycle.ViewModel;

public class PropertyDetailViewModel extends ViewModel {
    private final PropertyRepository repository;

//    trạng thái của dữ liệu (loading / success / error)
    private final MutableLiveData<Resource<PropertyDetailResponse>> detailState = new MutableLiveData<>();

    public PropertyDetailViewModel(PropertyRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<PropertyDetailResponse>> getDetailState() {
        return detailState;
    }

    public void loadDetail(Long id) {
        repository.getPropertyDetail(id, detailState);
    }

    public void loadDetail(Long id, String checkIn, String checkOut, Integer guests) {
        repository.getPropertyDetail(id, checkIn, checkOut, guests, detailState);
    }
}
