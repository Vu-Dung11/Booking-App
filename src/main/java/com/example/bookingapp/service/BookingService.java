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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import com.example.bookingapp.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final SecurityUtils securityUtils;

    // SELF-INJECTION: Dùng để gọi method nội bộ QUA Spring Proxy
    // Tránh vấn đề self-invocation khiến @Transactional bị bỏ qua
    @Lazy
    @Autowired
    private BookingService self;


    // BẮT BUỘC CÓ @Transactional để kích hoạt Pessimistic Lock và Rollback khi lỗi
    @Transactional
    public Booking createBooking(BookingRequest request) {
        // 1. Validate ngày tháng
        if (!request.getCheckIn().isBefore(request.getCheckOut())) {
            throw new AppException(ErrorCode.CHECK_OUT_MUST_BE_AFTER_CHECK_IN); // "Ngày trả phòng phải sau ngày nhận
            // phòng"
        }

        User guest = securityUtils.getCurrentUser();
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT)); // Không tìm thấy phòng

        // 2. TÌM VÀ KHÓA CÁC BẢN GHI INVENTORY
        // Bất kỳ luồng nào khác cố gắng đọc các dòng inventory này để đặt phòng đều
        // phải chờ
        List<RoomInventory> inventories = inventoryRepository.findAndLockInventoryByRoomAndDates(
                room.getId(), request.getCheckIn(), request.getCheckOut());

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

    // REQUIRES_NEW: Mỗi booking hủy có transaction độc lập
    // Nếu 1 booking lỗi, các booking khác vẫn tiếp tục được xử lý
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelUnpaidBooking(Long bookingId) {
        // 1. Tìm đơn hàng
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null)
            return;

        // 2. KÍCH HOẠT HỦY: Chỉ hủy nếu đơn hàng vẫn đang ở trạng thái PENDING
        // (Tránh trường hợp khách vừa thanh toán xong ở phút 14 nhưng hệ thống vẫn gọi
        // hủy)
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            log.info("Đơn hàng {} không ở trạng thái PENDING (hiện tại: {}), bỏ qua.",
                    bookingId, booking.getStatus());
            return;
        }

        // Chuyển trạng thái sang CANCELLED
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // 3. HOÀN TRẢ LẠI PHÒNG VÀO INVENTORY
        // Lấy lại danh sách inventory đã bị trừ của đơn hàng này
        List<RoomInventory> lockInventories = inventoryRepository.findAndLockInventoryByRoomAndDates(
                booking.getRoom().getId(), booking.getCheckInDate(), booking.getCheckOutDate());

        // BUG FIX: Dùng booking.getRoomQuantity() thay vì
        // booking.getRoom().getQuantity()
        // getRoom().getQuantity() là TỔNG số phòng của loại phòng đó, không phải số
        // phòng khách đặt
        int roomsToRestore = booking.getRoomQuantity();

        for (RoomInventory inventory : lockInventories) {
            inventory.setAvailableCount(inventory.getAvailableCount() + roomsToRestore);
        }
        inventoryRepository.saveAll(lockInventories);

        log.info("Đã HỦY đơn hàng {} và hoàn trả {} phòng vào inventory thành công.", bookingId, roomsToRestore);
    }

    // Trong file BookingService.java

    @Transactional
    public void cleanupExpiredBookings() {
        // Xác định mốc thời gian: Hiện tại trừ đi 15 phút
        LocalDateTime timeoutLimit = LocalDateTime.now().minusMinutes(15);

        // 1. Tìm danh sách đơn hàng "mồ côi" (vẫn PENDING sau 15p)
        List<Booking> expiredBookings = bookingRepository.findAllByStatusAndCreatedAtBefore(
                Booking.BookingStatus.PENDING, timeoutLimit);

        if (expiredBookings.isEmpty())
            return;

        log.info("Phát hiện {} đơn hàng quá hạn cần xử lý dọn dẹp.", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            try {
                // Gọi qua self (Spring Proxy) thay vì this để @Transactional(REQUIRES_NEW) hoạt
                // động
                self.cancelUnpaidBooking(booking.getId());
            } catch (Exception e) {
                log.error("Lỗi khi dọn dẹp đơn hàng {}: {}", booking.getId(), e.getMessage());
            }
        }
    }

    @Transactional
    public void autoCompleteBookings() {
        LocalDate today = LocalDate.now();

        List<Booking> bookingsToComplete = bookingRepository
                .findAllByStatusAndCheckOutDateLessThanEqual(Booking.BookingStatus.CONFIRMED, today);
        for (Booking booking : bookingsToComplete) {
            booking.setStatus(Booking.BookingStatus.COMPLETED);
        }
        bookingRepository.saveAll(bookingsToComplete);

        log.info("Đã tự động chuyển {} đơn hàng sang trạng thái COMPLETED.", bookingsToComplete.size());
    }

    @Scheduled(cron = "0 0 14 * * ?")
    public void runAutoCheckout() {
        log.info("Bắt đầu tiến trình tự động Check-out các đơn hàng đến hạn...");
        self.autoCompleteBookings();
    }

    public void checkOutCompltedBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Lỗi: không tìm thấy booking"));
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED)
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        bookingRepository.save(booking);
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void runCleanup() {
        log.info("Bắt đầu tiến trình quét vét đơn hàng quá hạn (Cron Job)...");
        // Gọi qua self (Spring Proxy) để @Transactional trên cleanupExpiredBookings
        // hoạt động
        self.cleanupExpiredBookings();
        log.info("Kết thúc tiến trình quét vét.");
    }

}