package com.example.bookingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long propertyId;
    private String propertyName;
    private Long bookingId;
    private String guestName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
