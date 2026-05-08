package com.example.bookingapp.controller;

import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.dto.HostDashboardResponse;
import com.example.bookingapp.service.HostDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/host/dashboard")
@PreAuthorize("hasRole('HOST')")
@RequiredArgsConstructor
public class HostDashboardController {

    private final HostDashboardService hostDashboardService;

    @GetMapping("/stats")
    public ApiResponse<HostDashboardResponse> stats() {
        return ApiResponse.success(hostDashboardService.getStatsForCurrentHost());
    }
}
