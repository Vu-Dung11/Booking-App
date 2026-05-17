package com.example.bookingapp.presentation.features.booking;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.PageResponse;
import com.example.bookingapp.data.model.booking.Booking;
import com.example.bookingapp.data.repository.BookingRepository;

public class BookingViewModel extends ViewModel {
    private final BookingRepository repository;
    private final MutableLiveData<Resource<PageResponse<Booking>>> bookingsState = new MutableLiveData<>();
    private String currentStatus = null; // null = tất cả

    public BookingViewModel(BookingRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<PageResponse<Booking>>> getBookingsState() {
        return bookingsState;
    }

    public String getCurrentStatus() { return currentStatus; }

    public void loadBookings(String status) {
        this.currentStatus = status;
        repository.getBookings(status, 0, 50, bookingsState);
    }

    public void refresh() {
        loadBookings(currentStatus);
    }
}
