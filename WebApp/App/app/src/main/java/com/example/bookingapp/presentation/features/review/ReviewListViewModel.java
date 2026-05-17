package com.example.bookingapp.presentation.features.review;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.PageResponse;
import com.example.bookingapp.data.model.review.ReviewResponse;
import com.example.bookingapp.data.model.review.ReviewSummaryResponse;
import com.example.bookingapp.data.repository.ReviewRepository;

import java.util.ArrayList;
import java.util.List;

public class ReviewListViewModel extends ViewModel {
    private final ReviewRepository repository;

    private final MutableLiveData<Resource<ReviewSummaryResponse>> summaryState = new MutableLiveData<>();
    private final MutableLiveData<Resource<PageResponse<ReviewResponse>>> pageState = new MutableLiveData<>();
    private final MutableLiveData<List<ReviewResponse>> accumulated = new MutableLiveData<>(new ArrayList<>());

    private Long propertyId;
    private int currentPage = 0;
    private int pageSize = 10;
    private boolean hasMore = true;
    private boolean loading = false;

    public ReviewListViewModel(ReviewRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<ReviewSummaryResponse>> getSummaryState() { return summaryState; }
    public LiveData<Resource<PageResponse<ReviewResponse>>> getPageState() { return pageState; }
    public LiveData<List<ReviewResponse>> getAccumulated() { return accumulated; }

    public boolean hasMore() { return hasMore; }
    public boolean isLoading() { return loading; }

    public void loadInitial(Long propertyId) {
        this.propertyId = propertyId;
        this.currentPage = 0;
        this.hasMore = true;
        this.accumulated.setValue(new ArrayList<>());
        repository.getReviewSummary(propertyId, summaryState);
        fetchPage();
    }

    public void loadMore() {
        if (loading || !hasMore || propertyId == null) return;
        currentPage++;
        fetchPage();
    }

    private void fetchPage() {
        loading = true;
        MutableLiveData<Resource<PageResponse<ReviewResponse>>> tmp = new MutableLiveData<>();
        tmp.observeForever(new androidx.lifecycle.Observer<Resource<PageResponse<ReviewResponse>>>() {
            @Override
            public void onChanged(Resource<PageResponse<ReviewResponse>> resource) {
                pageState.setValue(resource);
                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    List<ReviewResponse> cur = accumulated.getValue();
                    if (cur == null) cur = new ArrayList<>();
                    List<ReviewResponse> next = new ArrayList<>(cur);
                    if (resource.data.getContent() != null) next.addAll(resource.data.getContent());
                    accumulated.setValue(next);
                    hasMore = !resource.data.isLast();
                    loading = false;
                    tmp.removeObserver(this);
                } else if (resource.status == Resource.Status.ERROR) {
                    loading = false;
                    tmp.removeObserver(this);
                }
            }
        });
        repository.getReviewsByProperty(propertyId, currentPage, pageSize, tmp);
    }
}
