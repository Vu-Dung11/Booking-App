package com.example.bookingapp.dto;


import lombok.Builder;
import lombok.Getter;


import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class PropertySearchResponse {
    private Long propertyId;
    private String propertyName;
    private String address;
    private String city;
    private BigDecimal minPrice; // Giá thấp nhất của homestay đó
    private List<RoomSearchResponse> availableRooms;
}


