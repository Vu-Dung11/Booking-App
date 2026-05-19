package com.example.bookingapp.presentation.features.booking;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.booking.Booking;
import com.example.bookingapp.data.model.booking.BookingRequest;
import com.example.bookingapp.data.model.views.PropertyDetailResponse;
import com.example.bookingapp.data.repository.BookingRepository;
import com.example.bookingapp.data.repository.PropertyRepository;

public class BookingCreateViewModel extends ViewModel {
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;

    private final MutableLiveData<Resource<PropertyDetailResponse>> detailState = new MutableLiveData<>();
    private final MutableLiveData<Resource<Booking>> createState = new MutableLiveData<>();
    private final MutableLiveData<Resource<java.util.List<String>>> roomImagesState = new MutableLiveData<>();

    public BookingCreateViewModel(PropertyRepository propertyRepository, BookingRepository bookingRepository) {
        this.propertyRepository = propertyRepository;
        this.bookingRepository = bookingRepository;
    }

    public LiveData<Resource<PropertyDetailResponse>> getDetailState() { return detailState; }
    public LiveData<Resource<Booking>> getCreateState() { return createState; }
    public LiveData<Resource<java.util.List<String>>> getRoomImagesState() { return roomImagesState; }

    public void loadRoomImages(Long roomId) {
        propertyRepository.getRoomImages(roomId, roomImagesState);
    }

    public void loadDetail(Long propertyId) {
        propertyRepository.getPropertyDetail(propertyId, detailState);
    }

    public void loadDetail(Long propertyId, String checkIn, String checkOut, Integer guests) {
        propertyRepository.getPropertyDetail(propertyId, checkIn, checkOut, guests, detailState);
    }

    public void submit(BookingRequest req) {
        bookingRepository.createBooking(req, createState);
    }
}
