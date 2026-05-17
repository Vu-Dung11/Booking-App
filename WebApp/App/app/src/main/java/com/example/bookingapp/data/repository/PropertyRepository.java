package com.example.bookingapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.ApiResponse;
import com.example.bookingapp.data.model.PageResponse;
import com.example.bookingapp.data.model.views.PropertyDetailResponse;
import com.example.bookingapp.data.model.views.PropertyResponse;
import com.example.bookingapp.data.model.views.PropertySearchResponse;
import com.example.bookingapp.data.remote.ApiService;

import java.util.List;

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

    public void getPropertyDetail(Long id, MutableLiveData<Resource<PropertyDetailResponse>> state) {
        getPropertyDetail(id, null, null, null, state);
    }

    public void getPropertyDetail(Long id, String checkIn, String checkOut, Integer guests,
                                  MutableLiveData<Resource<PropertyDetailResponse>> state) {
        state.setValue(Resource.loading());
        apiService.getPropertyDetail(id, checkIn, checkOut, guests).enqueue(new Callback<ApiResponse<PropertyDetailResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PropertyDetailResponse>> call,
                                   Response<ApiResponse<PropertyDetailResponse>> response) {
                   if (response.isSuccessful() && response.body() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PropertyDetailResponse>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }

    public void searchProperties(String city, String checkIn, String checkOut, Integer guests,
                                 MutableLiveData<Resource<List<PropertySearchResponse>>> state) {
        state.setValue(Resource.loading());
        apiService.searchProperties(city, checkIn, checkOut, guests).enqueue(new Callback<ApiResponse<List<PropertySearchResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PropertySearchResponse>>> call,
                                   Response<ApiResponse<List<PropertySearchResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PropertySearchResponse>>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }

    public void getCities(MutableLiveData<Resource<List<String>>> state) {
        state.setValue(Resource.loading());
        apiService.getCities().enqueue(new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<String>>> call,
                                   Response<ApiResponse<List<String>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }
}
