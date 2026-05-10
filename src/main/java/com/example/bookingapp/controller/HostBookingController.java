package com.example.bookingapp.controller;

import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.dto.BookingDetailResponse;
import com.example.bookingapp.entity.Booking;
import com.example.bookingapp.form.CancelBookingRequest;
import com.example.bookingapp.form.ConfirmBookingRequest;
import com.example.bookingapp.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints quản lý booking dành cho HOST.
 * Read-only list/detail + manual confirm/cancel cho các đơn thanh toán offline
 * hoặc khách gọi điện huỷ.
 */
@RestController
@RequestMapping("/api/v1/host/bookings")
@PreAuthorize("hasRole('HOST')")
@RequiredArgsConstructor
public class HostBookingController {

    private final BookingService bookingService;

    @GetMapping
    public ApiResponse<Page<Booking>> list(
            @RequestParam(required = false) Booking.BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success(bookingService.getBookingsForCurrentHost(status, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingDetailResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(bookingService.getMyBookingDetail(id));
    }

    /** Host xác nhận thanh toán thủ công cho booking PENDING (CASH/BANK_TRANSFER/OTHER). */
    @PostMapping("/{id}/confirm")
    public ApiResponse<BookingDetailResponse> confirm(
            @PathVariable Long id,
            @RequestBody(required = false) ConfirmBookingRequest request) {
        return ApiResponse.success(bookingService.confirmMyBooking(id, request));
    }

    /** Host huỷ booking PENDING hoặc CONFIRMED. Hoàn lại inventory + đánh dấu payment REFUNDED. */
    @PostMapping("/{id}/cancel")
    public ApiResponse<BookingDetailResponse> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequest request) {
        return ApiResponse.success(bookingService.cancelMyBooking(id, request));
    }
}
