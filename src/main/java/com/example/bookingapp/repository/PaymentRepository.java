package com.example.bookingapp.repository;

import com.example.bookingapp.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** Lấy lịch sử thanh toán của 1 booking, mới nhất trước. */
    List<Payment> findByBooking_IdOrderByCreatedAtDesc(Long bookingId);

    /** Tìm payment SUCCESS của booking để mark REFUNDED khi host huỷ. */
    Optional<Payment> findFirstByBooking_IdAndStatusOrderByCreatedAtDesc(Long bookingId, Payment.PaymentStatus status);
}
