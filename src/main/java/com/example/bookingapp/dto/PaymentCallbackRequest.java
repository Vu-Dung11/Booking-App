package com.example.bookingapp.dto;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class PaymentCallbackRequest {
    private Long bookingId;
    private String transactionId;
    private BigDecimal amount;
    private String status; // VD: "00" là thành công trong VNPay
}