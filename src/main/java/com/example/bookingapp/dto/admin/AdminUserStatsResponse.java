package com.example.bookingapp.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserStatsResponse {
    private long totalUsers;
    private long totalAdmins;
    private long totalHosts;
    private long totalGuests;
    private long lockedUsers;
    private long activeUsers;
}
