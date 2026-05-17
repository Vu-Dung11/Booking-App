package com.example.bookingapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.ApiResponse;
import com.example.bookingapp.data.model.PageResponse;
import com.example.bookingapp.data.model.booking.Booking;
import com.example.bookingapp.data.model.booking.BookingRequest;
import com.example.bookingapp.data.remote.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingRepository {
    private final ApiService apiService;

    public BookingRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public void getBookings(String status, int page, int size,
                            MutableLiveData<Resource<PageResponse<Booking>>> state) {
        state.setValue(Resource.loading());
        apiService.getBookings(status, page, size).enqueue(new Callback<ApiResponse<PageResponse<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<Booking>>> call,
                                   Response<ApiResponse<PageResponse<Booking>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<Booking>>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }

    public void getBookingById(Long id, MutableLiveData<Resource<Booking>> state) {
        state.setValue(Resource.loading());
        apiService.getBookingById(id).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }

    public void createBooking(BookingRequest request, MutableLiveData<Resource<Booking>> state) {
        state.setValue(Resource.loading());
        apiService.createBooking(request).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    String msg = response.body() != null && response.body().getMessage() != null
                            ? response.body().getMessage()
                            : "Lỗi HTTP: " + response.code();
                    state.setValue(Resource.error(msg, null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }

    public void completeBooking(Long id, MutableLiveData<Resource<String>> state) {
        state.setValue(Resource.loading());
        apiService.completeBooking(id).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }

    public void cancelBooking(Long id, MutableLiveData<Resource<Booking>> state) {
        state.setValue(Resource.loading());
        apiService.cancelBooking(id).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }

    public void notifyPaymentSuccess(Long bookingId, String transactionId, java.math.BigDecimal amount,
                                      MutableLiveData<Resource<String>> state) {
        state.setValue(Resource.loading());
        com.example.bookingapp.data.model.payment.PaymentCallbackRequest body =
                new com.example.bookingapp.data.model.payment.PaymentCallbackRequest(
                        bookingId, transactionId, amount, "SUCCESS");
        apiService.paymentCallback(body).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }

    public void getVnpayUrl(Long bookingId, MutableLiveData<Resource<String>> state) {
        state.setValue(Resource.loading());
        apiService.getVnpayUrl(bookingId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }
}
