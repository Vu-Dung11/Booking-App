package com.example.bookingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertySummaryResponse {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String thumbnailUrl;
    private BigDecimal minPrice;
    private Double averageRating;
    private Long reviewCount;
}
