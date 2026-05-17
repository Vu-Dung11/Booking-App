package com.example.bookingapp.controller;

import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.entity.Booking;
import com.example.bookingapp.form.BookingRequest;
import com.example.bookingapp.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('GUEST')")
    public ApiResponse<Booking> createBooking(@Valid @RequestBody BookingRequest request) {
        Booking booking = bookingService.createBooking(request);
        return ApiResponse.success(booking);
    }

    @PostMapping("/{id}/booking-completed")
    public ApiResponse<String> confirmCheckOut(@PathVariable Long id) {
        bookingService.checkOutCompltedBooking(id);
        return ApiResponse.success("Confirm Check Out Successfully");
    }

    /** Admin xem toàn bộ booking trên hệ thống. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<Booking>> getAllBookings(
            @RequestParam(required = false) Booking.BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(bookingService.getAllBookings(status, pageable));
    }

    /** Guest xem booking của chính mình. */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<Booking>> getMyBookings(
            @RequestParam(required = false) Booking.BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(bookingService.getBookingsForCurrentGuest(status, pageable));
    }

    /** Detail có check quyền: guest-owner / host của property / admin. */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Booking> getBookingById(@PathVariable Long id) {
        return ApiResponse.success(bookingService.getBookingForCurrentGuest(id));
    }

    /** Guest hủy đơn PENDING của chính mình. */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Booking> cancelMyBooking(@PathVariable Long id) {
        return ApiResponse.success(bookingService.cancelMyPendingBooking(id));
    }
}
