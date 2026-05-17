package com.example.bookingapp.presentation.features.booking;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.booking.Booking;
import com.example.bookingapp.data.repository.BookingRepository;

public class BookingDetailViewModel extends ViewModel {
    private final BookingRepository repository;
    private final MutableLiveData<Resource<Booking>> bookingState = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> completeState = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> paymentUrlState = new MutableLiveData<>();
    private final MutableLiveData<Resource<Booking>> cancelState = new MutableLiveData<>();

    public BookingDetailViewModel(BookingRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<Booking>> getBookingState() { return bookingState; }
    public LiveData<Resource<String>> getCompleteState() { return completeState; }
    public LiveData<Resource<String>> getPaymentUrlState() { return paymentUrlState; }
    public LiveData<Resource<Booking>> getCancelState() { return cancelState; }

    public void cancelBooking(Long id) { repository.cancelBooking(id, cancelState); }

    private final MutableLiveData<Resource<String>> callbackState = new MutableLiveData<>();
    public LiveData<Resource<String>> getCallbackState() { return callbackState; }

    public void notifyPaymentSuccess(Long bookingId, String transactionId, java.math.BigDecimal amount) {
        repository.notifyPaymentSuccess(bookingId, transactionId, amount, callbackState);
    }

    public void loadBooking(Long id) {
        repository.getBookingById(id, bookingState);
    }

    public void completeBooking(Long id) {
        repository.completeBooking(id, completeState);
    }

    public void requestVnpayUrl(Long bookingId) {
        repository.getVnpayUrl(bookingId, paymentUrlState);
    }
}
