package com.example.bookingapp.repository;

import com.example.bookingapp.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByStatusAndCreatedAtBefore(Booking.BookingStatus status, LocalDateTime timeout);
    // Tìm các đơn hàng đã xác nhận nhưng ngày Check-out đã qua (hoặc bằng ngày hiện tại)
    List<Booking> findAllByStatusAndCheckOutDateLessThanEqual(Booking.BookingStatus status, LocalDate date);

}
