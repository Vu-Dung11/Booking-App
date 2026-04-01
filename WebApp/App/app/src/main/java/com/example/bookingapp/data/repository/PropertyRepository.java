package com.example.bookingapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.ApiResponse;
import com.example.bookingapp.data.model.PageResponse;
import com.example.bookingapp.data.model.views.PropertyResponse;
import com.example.bookingapp.data.remote.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PropertyRepository {
    private final ApiService apiService;

    public PropertyRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public void getAllProperties(int page, int size,
                                 MutableLiveData<Resource<PageResponse<PropertyResponse>>> state) {
        state.setValue(Resource.loading());
        apiService.getAllProperties(page, size).enqueue(new Callback<ApiResponse<PageResponse<PropertyResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<PropertyResponse>>> call,
                                   Response<ApiResponse<PageResponse<PropertyResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<PropertyResponse>>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }
}
