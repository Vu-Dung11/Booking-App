package com.example.bookingapp.controller;

import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.dto.admin.AdminReviewDetailResponse;
import com.example.bookingapp.dto.admin.AdminReviewResponse;
import com.example.bookingapp.service.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping
    public ApiResponse<Page<AdminReviewResponse>> list(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Long propertyId,
            @RequestParam(required = false) Long guestId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(
                adminReviewService.search(rating, propertyId, guestId, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminReviewDetailResponse> getDetail(@PathVariable Long id) {
        return ApiResponse.success(adminReviewService.getDetail(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        adminReviewService.delete(id);
        return ApiResponse.success("Đã xoá review");
    }
}
