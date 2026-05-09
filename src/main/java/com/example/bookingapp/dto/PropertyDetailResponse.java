package com.example.bookingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDetailResponse {
    private Long propetyId;
    private String name;
    private String description;
    private String address;
    private String city;
    private String country;
    private Boolean isActive;
    private String thumbnailUrl;
    private List<RoomSearchResponse> rooms;
}
