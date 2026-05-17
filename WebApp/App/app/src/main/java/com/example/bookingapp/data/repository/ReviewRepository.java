package com.example.bookingapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.ApiResponse;
import com.example.bookingapp.data.model.PageResponse;
import com.example.bookingapp.data.model.review.ReviewRequest;
import com.example.bookingapp.data.model.review.ReviewResponse;
import com.example.bookingapp.data.model.review.ReviewSummaryResponse;
import com.example.bookingapp.data.remote.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewRepository {
    private final ApiService apiService;

    public ReviewRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public void getReviewsByProperty(Long propertyId, int page, int size,
                                     MutableLiveData<Resource<PageResponse<ReviewResponse>>> state) {
        state.setValue(Resource.loading());
        apiService.getReviewsByProperty(propertyId, page, size).enqueue(new Callback<ApiResponse<PageResponse<ReviewResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<ReviewResponse>>> call,
                                   Response<ApiResponse<PageResponse<ReviewResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<ReviewResponse>>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }

    public void getReviewSummary(Long propertyId,
                                 MutableLiveData<Resource<ReviewSummaryResponse>> state) {
        state.setValue(Resource.loading());
        apiService.getReviewSummary(propertyId).enqueue(new Callback<ApiResponse<ReviewSummaryResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ReviewSummaryResponse>> call,
                                   Response<ApiResponse<ReviewSummaryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ReviewSummaryResponse>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }

    public void createReview(ReviewRequest request,
                             MutableLiveData<Resource<String>> state) {
        state.setValue(Resource.loading());
        apiService.createReview(request).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    String msg = "Lỗi HTTP: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String body = response.errorBody().string();
                            if (body != null && !body.isEmpty()) msg = body;
                        }
                    } catch (Exception ignored) {}
                    state.setValue(Resource.error(msg, null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }

    public void checkReviewExists(Long bookingId,
                                  MutableLiveData<Resource<Boolean>> state) {
        state.setValue(Resource.loading());
        apiService.checkReviewExists(bookingId).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }
}
