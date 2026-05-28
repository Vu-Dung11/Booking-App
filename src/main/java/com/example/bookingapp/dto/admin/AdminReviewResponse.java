package com.example.bookingapp.dto.admin;

import com.example.bookingapp.entity.Review;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminReviewResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    private Long bookingId;

    private Long propertyId;
    private String propertyName;

    private Long guestId;
    private String guestEmail;
    private String guestFullName;

    public static AdminReviewResponse fromEntity(Review r) {
        if (r == null) return null;
        return AdminReviewResponse.builder()
                .id(r.getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .bookingId(r.getBooking() != null ? r.getBooking().getId() : null)
                .propertyId(r.getProperty() != null ? r.getProperty().getId() : null)
                .propertyName(r.getProperty() != null ? r.getProperty().getName() : null)
                .guestId(r.getBooking() != null && r.getBooking().getGuest() != null
                        ? r.getBooking().getGuest().getId() : null)
                .guestEmail(r.getBooking() != null && r.getBooking().getGuest() != null
                        ? r.getBooking().getGuest().getEmail() : null)
                .guestFullName(r.getBooking() != null && r.getBooking().getGuest() != null
                        ? r.getBooking().getGuest().getFullName() : null)
                .build();
    }
}
