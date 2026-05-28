package com.example.bookingapp.controller;

import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.dto.admin.AdminPropertyDetailResponse;
import com.example.bookingapp.dto.admin.AdminPropertyResponse;
import com.example.bookingapp.service.AdminPropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/properties")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPropertyController {

    private final AdminPropertyService adminPropertyService;

    @GetMapping
    public ApiResponse<Page<AdminPropertyResponse>> list(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long hostId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(
                adminPropertyService.search(city, hostId, isActive, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminPropertyDetailResponse> getDetail(@PathVariable Long id) {
        return ApiResponse.success(adminPropertyService.getDetail(id));
    }

    @PatchMapping("/{id}/toggle-active")
    public ApiResponse<AdminPropertyResponse> toggleActive(@PathVariable Long id) {
        return ApiResponse.success(adminPropertyService.toggleActive(id));
    }

    /** Soft delete — set isActive=false, giữ history. */
    @DeleteMapping("/{id}")
    public ApiResponse<String> softDelete(@PathVariable Long id) {
        adminPropertyService.softDelete(id);
        return ApiResponse.success("Đã ẩn homestay khỏi hệ thống");
    }
}
