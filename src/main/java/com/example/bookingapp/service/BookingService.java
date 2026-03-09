package com.example.bookingapp.service;

import com.example.bookingapp.enm.ErrorCode;
import com.example.bookingapp.entity.Booking;
import com.example.bookingapp.entity.Room;
import com.example.bookingapp.entity.RoomInventory;
import com.example.bookingapp.entity.User;
import com.example.bookingapp.exception.AppException;
import com.example.bookingapp.form.BookingRequest;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.RoomInventoryRepository;
import com.example.bookingapp.repository.RoomRepository;
import com.example.bookingapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomInventoryRepository inventoryRepository;
    private final RoomRepository roomRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;

    private User getCurrentGuest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication==null){
            throw new AppException(ErrorCode.USER_NOT_AUTHENTICATED);
        }
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    // BẮT BUỘC CÓ @Transactional để kích hoạt Pessimistic Lock và Rollback khi lỗi
    @Transactional
    public Booking createBooking(BookingRequest request) {
        // 1. Validate ngày tháng
        if (!request.getCheckIn().isBefore(request.getCheckOut())) {
            throw new AppException(ErrorCode.CHECK_OUT_MUST_BE_AFTER_CHECK_IN); // "Ngày trả phòng phải sau ngày nhận phòng"
        }

        User guest = getCurrentGuest();
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT)); // Không tìm thấy phòng

        // 2. TÌM VÀ KHÓA CÁC BẢN GHI INVENTORY
        // Bất kỳ luồng nào khác cố gắng đọc các dòng inventory này để đặt phòng đều phải chờ
        List<RoomInventory> inventories = inventoryRepository.findAndLockInventoryByRoomAndDates(
                room.getId(), request.getCheckIn(), request.getCheckOut()
        );

        // 3. Kiểm tra xem có đủ dữ liệu lịch cho tất cả các đêm không
        long duration = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        if (inventories.size() != duration) {
            throw new AppException(ErrorCode.ROOM_FULLY_BOOKED); // Nhà cung cấp chưa mở lịch cho ngày này
        }

        // 4. Kiểm tra xem số lượng phòng trống của từng đêm có đủ cho khách không
        for (RoomInventory inv : inventories) {
            if (inv.getAvailableCount() < request.getRoomQuantity()) {
                throw new AppException(ErrorCode.ROOM_FULLY_BOOKED);
            }
        }

        // 5. Thỏa mãn hết điều kiện -> Trừ số lượng phòng trong kho
        for (RoomInventory inv : inventories) {
            inv.setAvailableCount(inv.getAvailableCount() - request.getRoomQuantity());
        }
        inventoryRepository.saveAll(inventories);

        // 6. Tính tổng tiền = (Giá phòng * số phòng đặt * số đêm)
        BigDecimal totalPrice = room.getBasePrice()
                .multiply(BigDecimal.valueOf(request.getRoomQuantity()))
                .multiply(BigDecimal.valueOf(duration));

        // 7. Tạo đơn hàng PENDING
        Booking booking = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(request.getCheckIn())
                .checkOutDate(request.getCheckOut())
                .totalPrice(totalPrice)
                .status(Booking.BookingStatus.PENDING)
                .roomQuantity(request.getRoomQuantity())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        String redisKey = "booking:timeout:" + savedBooking.getId();
        redisTemplate.opsForValue().set(redisKey, "PENDING", 15, TimeUnit.MINUTES);

        log.info("Đã đưa Booking ID {} vào hàng đợi chờ thanh toán 15 phút.", savedBooking.getId());
        log.info("Khách {} đã tạo thành công đơn đặt phòng PENDING ID: {}", guest.getEmail(), savedBooking.getId());

        // Khi hàm này kết thúc -> Transaction commit -> Tự động nhả khóa (Release Lock)
        return savedBooking;
    }
}