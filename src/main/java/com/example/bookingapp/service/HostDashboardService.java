package com.example.bookingapp.service;

import com.example.bookingapp.configuration.utils.SecurityUtils;
import com.example.bookingapp.dto.HostDashboardResponse;
import com.example.bookingapp.entity.Booking;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.PropertyRepository;
import com.example.bookingapp.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class HostDashboardService {

    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public HostDashboardResponse getStatsForCurrentHost() {
        Long hostId = securityUtils.getCurrentUser().getId();

        long totalProperties = propertyRepository.countByHostId(hostId);
        long activeProperties = propertyRepository.countByHostIdAndIsActive(hostId, true);

        long totalBookings = bookingRepository.countByRoom_Property_HostId(hostId);
        long pending = bookingRepository.countByRoom_Property_HostIdAndStatus(hostId, Booking.BookingStatus.PENDING);
        long confirmed = bookingRepository.countByRoom_Property_HostIdAndStatus(hostId, Booking.BookingStatus.CONFIRMED);
        long completed = bookingRepository.countByRoom_Property_HostIdAndStatus(hostId, Booking.BookingStatus.COMPLETED);
        long cancelled = bookingRepository.countByRoom_Property_HostIdAndStatus(hostId, Booking.BookingStatus.CANCELLED);

        long totalReviews = reviewRepository.countByProperty_HostId(hostId);
        Double avgRating = reviewRepository.averageRatingByHostId(hostId);
        BigDecimal revenue = bookingRepository.sumRevenueByHostId(hostId);

        return HostDashboardResponse.builder()
                .totalProperties(totalProperties)
                .activeProperties(activeProperties)
                .totalBookings(totalBookings)
                .pendingBookings(pending)
                .confirmedBookings(confirmed)
                .completedBookings(completed)
                .cancelledBookings(cancelled)
                .totalReviews(totalReviews)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
                .build();
    }
}
