package com.example.bookingapp.data.model.booking;

import com.example.bookingapp.data.model.views.RoomResponse;

import java.math.BigDecimal;

public class Booking {
    private Long id;
    private Guest guest;
    private Room room;
    private String checkInDate;   // yyyy-MM-dd
    private String checkOutDate;  // yyyy-MM-dd
    private BigDecimal totalPrice;
    private Integer roomQuantity;
    private String status;        // PENDING|CONFIRMED|CANCELLED|COMPLETED
    private String createdAt;

    public Long getId() { return id; }
    public Guest getGuest() { return guest; }
    public Room getRoom() { return room; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public Integer getRoomQuantity() { return roomQuantity; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }

    public static class Guest {
        private Long id;
        private String email;
        private String fullName;
        private String phoneNumber;

        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public String getPhoneNumber() { return phoneNumber; }
    }

    public static class Room {
        private Long id;
        private String roomType;
        private Integer capacity;
        private BigDecimal basePrice;
        private Integer quantity;
        private Property property;

        public Long getId() { return id; }
        public String getRoomType() { return roomType; }
        public Integer getCapacity() { return capacity; }
        public BigDecimal getBasePrice() { return basePrice; }
        public Integer getQuantity() { return quantity; }
        public Property getProperty() { return property; }
    }

    public static class Property {
        private Long id;
        private String name;
        private String address;
        private String city;
        private String country;

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getCity() { return city; }
        public String getCountry() { return country; }
    }
}
