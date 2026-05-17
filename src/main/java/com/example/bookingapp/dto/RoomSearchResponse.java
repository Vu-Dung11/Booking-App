package com.example.bookingapp.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RoomSearchResponse {
    private Long roomId;
    private String roomType;
    private BigDecimal price;
    private Integer capacity;
    private Integer quantity;
    private String thumbnailUrl;

    /**
     * Số phòng tối thiểu còn trống trong khoảng [checkIn, checkOut) mà client truyền.
     * NULL khi endpoint không nhận date params (giữ behavior cũ).
     */
    private Integer availableCount;
}