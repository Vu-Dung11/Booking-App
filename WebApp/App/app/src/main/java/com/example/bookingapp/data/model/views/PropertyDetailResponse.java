package com.example.bookingapp.data.model.views;

import java.util.List;

public class PropertyDetailResponse {
    private Long propertyId;
    private String name;
    private String description;
    private String address;
    private String city;
    private String country;
    private List<RoomResponse> rooms;

    public Long getPropertyId() {
        return propertyId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public List<RoomResponse> getRooms() {
        return rooms;
    }
}
