package com.example.bookingapp.service;

import com.example.bookingapp.dto.admin.AdminDashboardStatsResponse;
import com.example.bookingapp.dto.admin.AdminRecentActivityResponse;
import com.example.bookingapp.entity.Booking;
import com.example.bookingapp.entity.Review;
import com.example.bookingapp.entity.User;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.PropertyRepository;
import com.example.bookingapp.repository.ReviewRepository;
import com.example.bookingapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    public AdminDashboardStatsResponse getStats() {
        long totalAdmins = userRepository.countByRole(User.Role.ADMIN);
        long totalHosts = userRepository.countByRole(User.Role.HOST);
        long totalGuests = userRepository.countByRole(User.Role.GUEST);
        long locked = userRepository.countByIsActive(false);

        long activeProps = propertyRepository.countByIsActive(true);
        long inactiveProps = propertyRepository.countByIsActive(false);

        long pending = bookingRepository.countByStatus(Booking.BookingStatus.PENDING);
        long confirmed = bookingRepository.countByStatus(Booking.BookingStatus.CONFIRMED);
        long completed = bookingRepository.countByStatus(Booking.BookingStatus.COMPLETED);
        long cancelled = bookingRepository.countByStatus(Booking.BookingStatus.CANCELLED);

        BigDecimal revenue = bookingRepository.sumTotalRevenue();
        if (revenue == null) revenue = BigDecimal.ZERO;

        return AdminDashboardStatsResponse.builder()
                .totalUsers(totalAdmins + totalHosts + totalGuests)
                .totalAdmins(totalAdmins)
                .totalHosts(totalHosts)
                .totalGuests(totalGuests)
                .lockedUsers(locked)
                .totalProperties(activeProps + inactiveProps)
                .activeProperties(activeProps)
                .inactiveProperties(inactiveProps)
                .totalBookings(pending + confirmed + completed + cancelled)
                .pendingBookings(pending)
                .confirmedBookings(confirmed)
                .completedBookings(completed)
                .cancelledBookings(cancelled)
                .totalReviews(reviewRepository.count())
                .totalRevenue(revenue)
                .build();
    }

    public AdminRecentActivityResponse getRecentActivities() {
        // User: không có createdAt -> dùng id desc làm proxy
        List<User> recentUsers = userRepository.findRecent(PageRequest.of(0, 10));
        List<AdminRecentActivityResponse.RecentItem> users = recentUsers.stream()
                .map(u -> AdminRecentActivityResponse.RecentItem.builder()
                        .type("USER")
                        .id(u.getId())
                        .title(u.getFullName() == null ? u.getEmail() : u.getFullName())
                        .subtitle(u.getRole() + " · " + u.getEmail())
                        .createdAt(null)
                        .build())
                .toList();

        List<Booking> recentBookings = bookingRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10))
                .getContent();
        List<AdminRecentActivityResponse.RecentItem> bookings = recentBookings.stream()
                .map(b -> AdminRecentActivityResponse.RecentItem.builder()
                        .type("BOOKING")
                        .id(b.getId())
                        .title("Booking #" + b.getId() + " · " + b.getStatus())
                        .subtitle(b.getGuest() != null ? b.getGuest().getEmail() : null)
                        .createdAt(b.getCreatedAt())
                        .build())
                .toList();

        List<Review> recentReviews = reviewRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10))
                .getContent();
        List<AdminRecentActivityResponse.RecentItem> reviews = recentReviews.stream()
                .map(r -> AdminRecentActivityResponse.RecentItem.builder()
                        .type("REVIEW")
                        .id(r.getId())
                        .title("Review " + r.getRating() + "★ · "
                                + (r.getProperty() != null ? r.getProperty().getName() : ""))
                        .subtitle(r.getComment())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();

        return AdminRecentActivityResponse.builder()
                .recentUsers(users)
                .recentBookings(bookings)
                .recentReviews(reviews)
                .build();
    }
}
