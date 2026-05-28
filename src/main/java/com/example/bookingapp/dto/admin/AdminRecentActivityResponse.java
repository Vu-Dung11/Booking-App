package com.example.bookingapp.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminRecentActivityResponse {
    private List<RecentItem> recentUsers;
    private List<RecentItem> recentBookings;
    private List<RecentItem> recentReviews;

    @Data
    @Builder
    public static class RecentItem {
        /** "USER" | "BOOKING" | "REVIEW" */
        private String type;
        private Long id;
        private String title;
        private String subtitle;
        private LocalDateTime createdAt;
    }
}
