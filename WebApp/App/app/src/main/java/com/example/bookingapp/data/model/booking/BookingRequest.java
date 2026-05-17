package com.example.bookingapp.data.model.booking;

public class BookingRequest {
    private Long roomId;
    private String checkIn;       // yyyy-MM-dd
    private String checkOut;      // yyyy-MM-dd
    private Integer roomQuantity;

    public BookingRequest(Long roomId, String checkIn, String checkOut, Integer roomQuantity) {
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.roomQuantity = roomQuantity;
    }

    public Long getRoomId() { return roomId; }
    public String getCheckIn() { return checkIn; }
    public String getCheckOut() { return checkOut; }
    public Integer getRoomQuantity() { return roomQuantity; }
}
