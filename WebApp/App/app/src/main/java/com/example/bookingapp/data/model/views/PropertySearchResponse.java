package com.example.bookingapp.data.model.views;

import java.math.BigDecimal;
import java.util.List;

public class PropertySearchResponse {
    private Long propertyId;
    private String propertyName;
    private String address;
    private String city;
    private BigDecimal minPrice;
    private List<RoomResponse> availableRooms;

    public Long getPropertyId() { return propertyId; }
    public String getPropertyName() { return propertyName; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public BigDecimal getMinPrice() { return minPrice; }
    public List<RoomResponse> getAvailableRooms() { return availableRooms; }
}
