package com.example.bookingapp.service;

import com.example.bookingapp.entity.Booking;
import com.example.bookingapp.entity.RoomInventory;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.RoomInventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Component
public class BookingExpirationListener extends KeyExpirationEventMessageListener {
    private final BookingRepository bookingRepository;
    private final RoomInventoryRepository inventoryRepository;

    public BookingExpirationListener(RedisMessageListenerContainer listenerContainer,
                                     BookingRepository bookingRepository,
                                     RoomInventoryRepository inventoryRepository) {
        super(listenerContainer);
        this.bookingRepository = bookingRepository;
        this.inventoryRepository = inventoryRepository;
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

            cancelUnpaidBooking(bookingId);
        }
    }

    private void cancelUnpaidBooking(Long bookingId) {
        // 1. Tìm đơn hàng
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return;

        // 2. KÍCH HOẠT HỦY: Chỉ hủy nếu đơn hàng vẫn đang ở trạng thái PENDING
        // (Tránh trường hợp khách vừa thanh toán xong ở phút 14 nhưng hệ thống vẫn gọi hủy)
        if (booking.getStatus() == Booking.BookingStatus.PENDING) {
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        }

        // 3. HOÀN TRẢ LẠI PHÒNG VÀO INVENTORY
        // Lấy lại danh sách inventory đã bị trừ của đơn hàng này
        List<RoomInventory> lockInventories = inventoryRepository.findAndLockInventoryByRoomAndDates(
                booking.getRoom().getId(), booking.getCheckInDate(), booking.getCheckOutDate()
        );
        // Cộng lại số lượng phòng
        // Cần lấy ra quantity từ booking
        // (bạn có thể lưu totalRoom trong entity Booking nếu đặt nhiều phòng,
        // ở đây giả sử mỗi booking chỉ đặt 1 số lượng phòng cố định, ví dụ: 1)
        int roomsToRestore = booking.getRoom().getQuantity();

        for (RoomInventory inventory : lockInventories) {
            inventory.setAvailableCount(inventory.getAvailableCount() + roomsToRestore);
        }
        inventoryRepository.saveAll(lockInventories);
        // Thay bằng booking.getRoomQuantity() nếu bạn có lưu cột này
        log.info("Đã HỦY đơn hàng {} và hoàn trả phòng thành công do quá hạn thanh toán.", bookingId);
    }
}

