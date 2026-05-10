package com.example.bookingapp.form;

import lombok.Getter;
import lombok.Setter;

/**
 * Request body cho host xác nhận thanh toán thủ công (offline).
 * Tất cả field optional. Default paymentMethod = CASH.
 */
@Getter
@Setter
public class ConfirmBookingRequest {
    /** CASH | BANK_TRANSFER | OTHER. Default CASH. */
    private String paymentMethod;

    /** Mã giao dịch tự nhập (số phiếu thu, mã CK...). Default auto-gen. */
    private String transactionId;

    /** Ghi chú nội bộ (chưa lưu DB, chỉ log). */
    private String note;
}
