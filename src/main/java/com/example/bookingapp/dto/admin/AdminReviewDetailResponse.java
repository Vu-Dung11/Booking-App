package com.example.bookingapp.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminReviewDetailResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    // Booking
    private Long bookingId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalPrice;

    // Property
    private Long propertyId;
    private String propertyName;
    private String propertyAddress;
    private String propertyCity;

    // Guest
    private Long guestId;
    private String guestEmail;
    private String guestFullName;
    private String guestPhone;
}
