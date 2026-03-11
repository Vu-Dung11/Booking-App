package com.example.bookingapp.controller;


import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.dto.PaymentCallbackRequest;
import com.example.bookingapp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // API nhận kết quả từ cổng thanh toán (IPN URL)
    @PostMapping("/callback")
    public ApiResponse<String> paymentCallback(@RequestBody PaymentCallbackRequest request) {
        paymentService.processPaymentCallback(request);
        return ApiResponse.success("Dữ liệu đã được xử lý");
    }
}