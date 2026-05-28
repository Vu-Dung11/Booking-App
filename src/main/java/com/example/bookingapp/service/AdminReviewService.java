package com.example.bookingapp.service;

import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.dto.admin.AdminReviewDetailResponse;
import com.example.bookingapp.dto.admin.AdminReviewResponse;
import com.example.bookingapp.entity.Booking;
import com.example.bookingapp.entity.Property;
import com.example.bookingapp.entity.Review;
import com.example.bookingapp.entity.User;
import com.example.bookingapp.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;

    public Page<AdminReviewResponse> search(Integer rating, Long propertyId, Long guestId,
                                            String keyword, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return reviewRepository.searchForAdmin(rating, propertyId, guestId, kw, pageable)
                .map(AdminReviewResponse::fromEntity);
    }

    public AdminReviewDetailResponse getDetail(Long id) {
        Review r = reviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        Booking b = r.getBooking();
        Property p = r.getProperty();
        User guest = (b != null) ? b.getGuest() : null;

        return AdminReviewDetailResponse.builder()
                .id(r.getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .bookingId(b != null ? b.getId() : null)
                .checkInDate(b != null ? b.getCheckInDate() : null)
                .checkOutDate(b != null ? b.getCheckOutDate() : null)
                .totalPrice(b != null ? b.getTotalPrice() : null)
                .propertyId(p != null ? p.getId() : null)
                .propertyName(p != null ? p.getName() : null)
                .propertyAddress(p != null ? p.getAddress() : null)
                .propertyCity(p != null ? p.getCity() : null)
                .guestId(guest != null ? guest.getId() : null)
                .guestEmail(guest != null ? guest.getEmail() : null)
                .guestFullName(guest != null ? guest.getFullName() : null)
                .guestPhone(guest != null ? guest.getPhoneNumber() : null)
                .build();
    }

    @Transactional
    public void delete(Long id) {
        Review r = reviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        reviewRepository.delete(r);
    }
}
