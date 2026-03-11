package com.example.bookingapp.repository;

import com.example.bookingapp.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByStatusAndCreatedAtBefore(Booking.BookingStatus status, LocalDateTime timeout);
}
