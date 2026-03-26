package com.example.bookingapp.controller;


import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.dto.PaymentCallbackRequest;
import com.example.bookingapp.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/callback")
    public ApiResponse<String> paymentCallback(@RequestBody PaymentCallbackRequest request) {
        paymentService.processPaymentCallback(request);
        return ApiResponse.success("Dữ liệu đã được xử lý");
    }
    @GetMapping("/vnpay-url")
    public ApiResponse<String> createPaymentUrl(@RequestParam Long bookingId, HttpServletRequest request) {
        String url = paymentService.createVnPayUrl(bookingId, request);
        return ApiResponse.success(url);
    }

    @GetMapping("/vnpay-return")
    public ApiResponse<String> vnpayReturn(HttpServletRequest request) {
        String result = paymentService.processVnPayReturn(request);
        return ApiResponse.success(result);
    }
}