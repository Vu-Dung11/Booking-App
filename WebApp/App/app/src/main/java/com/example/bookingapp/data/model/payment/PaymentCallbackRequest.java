package com.example.bookingapp.data.model.payment;

import java.math.BigDecimal;

public class PaymentCallbackRequest {
    private Long bookingId;
    private String transactionId;
    private BigDecimal amount;
    private String status; // "SUCCESS" để backend chuyển booking → CONFIRMED

    public PaymentCallbackRequest(Long bookingId, String transactionId, BigDecimal amount, String status) {
        this.bookingId = bookingId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.status = status;
    }

    public Long getBookingId() { return bookingId; }
    public String getTransactionId() { return transactionId; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
}
