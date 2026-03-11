package com.example.bookingapp.service;


import com.example.bookingapp.dto.PaymentCallbackRequest;
import com.example.bookingapp.enm.ErrorCode;
import com.example.bookingapp.entity.Booking;
import com.example.bookingapp.entity.Payment;
import com.example.bookingapp.exception.AppException;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void processPaymentCallback(PaymentCallbackRequest request) {
        // 1. Tìm đơn hàng
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT));

        // 2. Kiểm tra xem đơn hàng có đang đợi thanh toán không
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            log.warn("Đơn hàng {} đã được xử lý hoặc đã hủy trước đó.", booking.getId());
            return;
        }

        // 3. Nếu thanh toán thành công (giả lập status "SUCCESS")
        if ("SUCCESS".equals(request.getStatus())) {

            // Cập nhật trạng thái Booking
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            // Tạo bản ghi Payment
            Payment payment = Payment.builder()
                    .booking(booking)
                    .amount(request.getAmount())
                    .transactionId(request.getTransactionId())
                    .paymentMethod("VNPAY_MOCK")
                    .status(Payment.PaymentStatus.SUCCESS)
                    .createdAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);

            // 4. QUAN TRỌNG: Xóa Key timeout trong Redis để dừng việc tự động hủy đơn
            String redisKey = "booking:timeout:" + booking.getId();
            Boolean deleted = redisTemplate.delete(redisKey);

            if (Boolean.TRUE.equals(deleted)) {
                log.info("Đã xóa key timeout Redis cho đơn hàng {}. Đơn hàng an toàn!", booking.getId());
            }

            log.info("Thanh toán thành công cho đơn hàng {}", booking.getId());
        } else {
            // Xử lý khi thanh toán thất bại (nếu cần)
            log.error("Thanh toán thất bại cho đơn hàng {}", booking.getId());
        }
    }
}