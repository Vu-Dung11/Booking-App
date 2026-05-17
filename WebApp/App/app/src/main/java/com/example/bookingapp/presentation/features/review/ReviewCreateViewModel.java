package com.example.bookingapp.presentation.features.review;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.review.ReviewRequest;
import com.example.bookingapp.data.repository.ReviewRepository;

public class ReviewCreateViewModel extends ViewModel {
    private final ReviewRepository repository;

    private final MutableLiveData<Resource<String>> submitState = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> existsState = new MutableLiveData<>();

    public ReviewCreateViewModel(ReviewRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<String>> getSubmitState() { return submitState; }
    public LiveData<Resource<Boolean>> getExistsState() { return existsState; }

    public void submit(Long bookingId, int rating, String comment) {
        repository.createReview(new ReviewRequest(bookingId, rating, comment), submitState);
    }

    public void checkExists(Long bookingId) {
        repository.checkReviewExists(bookingId, existsState);
    }
}
