package com.example.bookingapp.controller;


import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.form.ReviewRequest;
import com.example.bookingapp.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ApiResponse<String> createReview(@Valid @RequestBody ReviewRequest reviewRequest) {
        reviewService.createReview(reviewRequest);
        return ApiResponse.success("Tạo review thành công");
    }

    @GetMapping("/check")
    public ApiResponse<Boolean> checkReviewExists(@RequestParam Long bookingId) {
        return ApiResponse.success(reviewService.existsByBooking(bookingId));
    }
}
