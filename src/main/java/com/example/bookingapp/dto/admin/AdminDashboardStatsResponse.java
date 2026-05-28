package com.example.bookingapp.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminDashboardStatsResponse {
    // Users
    private long totalUsers;
    private long totalAdmins;
    private long totalHosts;
    private long totalGuests;
    private long lockedUsers;

    // Properties
    private long totalProperties;
    private long activeProperties;
    private long inactiveProperties;

    // Bookings
    private long totalBookings;
    private long pendingBookings;
    private long confirmedBookings;
    private long completedBookings;
    private long cancelledBookings;

    // Reviews
    private long totalReviews;

    // Revenue (CONFIRMED + COMPLETED)
    private BigDecimal totalRevenue;
}
