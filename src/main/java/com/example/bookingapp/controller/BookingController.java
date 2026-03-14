package com.example.bookingapp.controller;

import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.entity.Booking;
import com.example.bookingapp.form.BookingRequest;
import com.example.bookingapp.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        // Nếu CheckOutCompltedBooking gặp lỗi và throw AppException,
        // GlobalExceptionHandler sẽ tự động "hứng" lỗi đó và trả về JSON lỗi phù hợp.
        bookingService.checkOutCompltedBooking(id);
        return ApiResponse.success("Confirm Check Out Successfully");
    }

}