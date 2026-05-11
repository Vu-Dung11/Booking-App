package com.example.bookingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Summary 1 booking đang giữ chỗ ở 1 ngày — dùng cho drill-down từ cell calendar.
 */
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BookingByDateItem {
    private Long id;
    private String guestName;
    private String guestPhone;
    private String status;
    private Integer roomQuantity;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
