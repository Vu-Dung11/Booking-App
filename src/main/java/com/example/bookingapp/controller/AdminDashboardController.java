package com.example.bookingapp.controller;

import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.dto.admin.AdminDashboardStatsResponse;
import com.example.bookingapp.dto.admin.AdminRecentActivityResponse;
import com.example.bookingapp.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    public ApiResponse<AdminDashboardStatsResponse> stats() {
        return ApiResponse.success(adminDashboardService.getStats());
    }

    @GetMapping("/recent-activities")
    public ApiResponse<AdminRecentActivityResponse> recentActivities() {
        return ApiResponse.success(adminDashboardService.getRecentActivities());
    }
}
