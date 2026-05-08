package com.example.bookingapp.controller;

import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.dto.ReviewResponse;
import com.example.bookingapp.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * HOST chỉ xem các đánh giá thuộc về property của mình.
 */
@RestController
@RequestMapping("/api/v1/host/reviews")
@PreAuthorize("hasRole('HOST')")
@RequiredArgsConstructor
public class HostReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ApiResponse<Page<ReviewResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success(reviewService.getReviewsForCurrentHost(pageable));
    }
}
