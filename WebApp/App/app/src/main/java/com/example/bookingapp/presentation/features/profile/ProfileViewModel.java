package com.example.bookingapp.presentation.features.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.user.UserResponse;
import com.example.bookingapp.data.repository.AuthRepository;

public class ProfileViewModel extends ViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<Resource<UserResponse>> meState = new MutableLiveData<>();

    public ProfileViewModel(AuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<UserResponse>> getMeState() { return meState; }

    public void loadMe() {
        repository.getMe(meState);
    }
}
