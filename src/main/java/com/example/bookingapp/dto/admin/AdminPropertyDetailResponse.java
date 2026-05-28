package com.example.bookingapp.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdminPropertyDetailResponse {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String country;
    private Boolean isActive;

    private Long hostId;
    private String hostEmail;
    private String hostFullName;
    private String hostPhone;

    private List<String> images;
    private List<RoomSummary> rooms;

    private Long totalReviews;
    private Double averageRating;
    private Long totalBookings;

    @Data
    @Builder
    public static class RoomSummary {
        private Long id;
        private String roomType;
        private Integer capacity;
        private BigDecimal basePrice;
        private Integer quantity;
        private String thumbnailUrl;
    }
}
