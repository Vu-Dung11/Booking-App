package com.example.bookingapp.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne // Một đơn đặt phòng thường ứng với một giao dịch thanh toán thành công
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    private BigDecimal amount;
    private String paymentMethod; // VNPay, Momo, v.v.

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String transactionId; // Mã giao dịch từ cổng thanh toán
    private LocalDateTime createdAt;

    public enum PaymentStatus {PENDING, SUCCESS, FAILED, REFUNDED}
}