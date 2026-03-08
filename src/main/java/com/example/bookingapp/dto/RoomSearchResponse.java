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
}