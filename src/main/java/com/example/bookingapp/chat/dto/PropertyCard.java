package com.example.bookingapp.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyCard {
    private Long propertyId;
    private String name;
    private String thumbnailUrl;
    private String city;
    private BigDecimal minPrice;
}
