package com.example.bookingapp.service;




import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.dto.ReviewResponse;
import com.example.bookingapp.dto.ReviewSummaryResponse;
import com.example.bookingapp.entity.Booking;
import com.example.bookingapp.entity.Property;
import com.example.bookingapp.entity.Review;
import com.example.bookingapp.entity.User;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.form.ReviewRequest;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.ReviewRepository;
import com.example.bookingapp.configuration.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public Review createReview(ReviewRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        // 1. Tìm đơn hàng
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // 2. Validate: Có đúng là khách của đơn hàng này không?
        if (!booking.getGuest().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.NOT_YOUR_BOOKING);
        }

        // 3. Validate: Đơn hàng đã COMPLETED chưa?
        if (booking.getStatus() != Booking.BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.BOOKING_IS_NOT_COMPLETED);
        }
        // 4. Validate: Mỗi đơn chỉ được đánh giá 1 lần
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new AppException(ErrorCode.EXISTED_REVIEW_FOR_BOOKING);
        }
        // 5. Tự động lấy Property_id từ Booking (Chống gửi ảo từ Frontend)
        Property property = booking.getRoom().getProperty();
        // 6. Build dữ liệu và Save vào DB
        Review review = Review.builder()
                .booking(booking)
                .property(property)  // Map đúng cột property_id trong DB
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        log.info("Khách hàng {} vừa đánh giá {} sao cho Homestay: {}",
                currentUser.getEmail(), request.getRating(), property.getName());
        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByProperty(Long propertyId, Pageable pageable) {
        return reviewRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getSummary(Long propertyId) {
        java.util.List<Review> all = reviewRepository.findByPropertyId(propertyId);
        java.util.Map<Integer, Long> dist = new java.util.HashMap<>();
        for (int i = 1; i <= 5; i++) dist.put(i, 0L);
        double sum = 0;
        for (Review r : all) {
            Integer rt = r.getRating();
            if (rt == null) continue;
            sum += rt;
            dist.merge(rt, 1L, Long::sum);
        }
        double avg = all.isEmpty() ? 0.0 : sum / all.size();
        return ReviewSummaryResponse.builder()
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .totalCount(all.size())
                .distribution(dist)
                .build();
    }

    @Transactional(readOnly = true)
    public boolean existsByBooking(Long bookingId) {
        return reviewRepository.existsByBookingId(bookingId);
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .propertyId(r.getProperty().getId())
                .propertyName(r.getProperty().getName())
                .bookingId(r.getBooking().getId())
                .guestName(r.getBooking().getGuest().getFullName())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }

    /** Trả về review của các property thuộc về host hiện tại, kèm thông tin khách + homestay. */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsForCurrentHost(Pageable pageable) {
        Long hostId = securityUtils.getCurrentUser().getId();
        return reviewRepository.findByProperty_HostId(hostId, pageable)
                .map(r -> ReviewResponse.builder()
                        .id(r.getId())
                        .propertyId(r.getProperty().getId())
                        .propertyName(r.getProperty().getName())
                        .bookingId(r.getBooking().getId())
                        .guestName(r.getBooking().getGuest().getFullName())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .createdAt(r.getCreatedAt())
                        .build());
    }
}
