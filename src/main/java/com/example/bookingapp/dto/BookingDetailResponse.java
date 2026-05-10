package com.example.bookingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Booking detail enriched cho host: bao gồm guest contact, property info,
 * payment history và pendingExpiresAt (lấy từ Redis TTL).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailResponse {
    private Long id;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalPrice;
    private Integer roomQuantity;
    private String status;
    private LocalDateTime createdAt;
    /** Thời điểm hết hạn 15-phút (chỉ với PENDING). NULL nếu không có TTL. */
    private LocalDateTime pendingExpiresAt;

    private GuestInfo guest;
    private RoomInfo room;
    private PropertyInfo property;
    private List<PaymentInfo> payments;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class GuestInfo {
        private Long id;
        private String fullName;
        private String email;
        private String phoneNumber;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RoomInfo {
        private Long id;
        private String roomType;
        private Integer capacity;
        private BigDecimal basePrice;
        private Integer quantity;
        private String thumbnailUrl;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PropertyInfo {
        private Long id;
        private String name;
        private String address;
        private String city;
        private String country;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PaymentInfo {
        private Long id;
        private BigDecimal amount;
        private String paymentMethod;
        private String status;
        private String transactionId;
        private LocalDateTime createdAt;
    }
}
