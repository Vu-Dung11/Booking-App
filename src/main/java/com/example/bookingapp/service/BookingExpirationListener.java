package com.example.bookingapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
// class for listen booking expiration
public class BookingExpirationListener extends KeyExpirationEventMessageListener {
    private final BookingService bookingService;

    public BookingExpirationListener(RedisMessageListenerContainer listenerContainer,
                                     BookingService bookingService) {
        super(listenerContainer);
        this.bookingService = bookingService;
    }

    @Override
    @Transactional// Bắt buộc có Transaction vì chúng ta sẽ update database
    public void onMessage(Message message, byte[] pattern) {
// Tên của Key vừa hết hạn (VD: "booking:timeout:123")
        String expiredKey = message.toString();

        if (expiredKey.startsWith("booking:timeout:")) {
            // Cắt chuỗi để lấy ra Booking ID
            Long bookingId = Long.parseLong(expiredKey.split(":")[2]);
            log.info("Nhận được tín hiệu hết hạn thanh toán từ Redis cho Booking ID: {}", bookingId);
            bookingService.cancelUnpaidBooking(bookingId);
        }
    }


}

