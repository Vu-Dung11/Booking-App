package com.example.bookingapp.controller;

import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.entity.Booking;
import com.example.bookingapp.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Read-only API: HOST chỉ xem được booking của các property thuộc mình.
 * Không có endpoint xác nhận check-out hay sửa booking — host không can thiệp đơn của khách.
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
    public ApiResponse<Booking> getById(@PathVariable Long id) {
        return ApiResponse.success(bookingService.getBookingForCurrentHost(id));
    }
}
