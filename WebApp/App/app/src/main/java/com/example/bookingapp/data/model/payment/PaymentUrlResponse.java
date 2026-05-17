package com.example.bookingapp.data.model.payment;

// Backend trả về String trong ApiResponse.data → dùng String trực tiếp ở ApiService.
// File này giữ chỗ cho mở rộng (vd: thêm transactionId).
public class PaymentUrlResponse {
    private String url;

    public PaymentUrlResponse(String url) { this.url = url; }
    public String getUrl() { return url; }
}
