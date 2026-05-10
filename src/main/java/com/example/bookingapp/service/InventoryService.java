package com.example.bookingapp.service;


import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.configuration.utils.SecurityUtils;
import com.example.bookingapp.dto.HostCalendarResponse;
import com.example.bookingapp.entity.Booking;
import com.example.bookingapp.entity.Property;
import com.example.bookingapp.entity.Room;
import com.example.bookingapp.entity.RoomInventory;
import com.example.bookingapp.entity.User;
import com.example.bookingapp.form.DayInventoryUpdateRequest;
import com.example.bookingapp.form.ExtendInventoryRequest;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.PropertyRepository;
import com.example.bookingapp.repository.RoomInventoryRepository;
import com.example.bookingapp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final RoomInventoryRepository inventoryRepository;
    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final SecurityUtils securityUtils;

    private static final int DAYS_IN_ADVANCE = 90;          // Mở lịch trước 90 ngày khi tạo room
    private static final int MAX_QUERY_RANGE_DAYS = 90;     // Calendar query không quá 90 ngày
    private static final int MAX_EXTEND_DAYS_AHEAD = 365;   // Extend không quá 1 năm vào tương lai

    /** Sinh kho phòng lần đầu tiên ngay khi Host tạo phòng mới. */
    @Transactional
    public void generateInitialInventory(Room room) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(DAYS_IN_ADVANCE);

        List<RoomInventory> inventories = new ArrayList<>();
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            RoomInventory inventory = RoomInventory.builder()
                    .room(room)
                    .inventoryDate(date)
                    .availableCount(room.getQuantity())
                    .build();
            inventories.add(inventory);
        }
        inventoryRepository.saveAll(inventories);
        log.info("Đã tạo thành công lịch 90 ngày cho phòng ID: {}", room.getId());
    }

    /** Cron Job mỗi ngày 00:00 — bổ sung lịch ngày thứ 90 cho mọi room. */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void maintainDailyInventory() {
        log.info("Bắt đầu chạy Job bảo trì kho phòng hàng ngày...");
        LocalDate targetDate = LocalDate.now().plusDays(DAYS_IN_ADVANCE - 1);
        List<Room> allRooms = roomRepository.findAll();
        List<RoomInventory> newInventories = new ArrayList<>();

        for (Room room : allRooms) {
            if (!inventoryRepository.existsByRoomIdAndInventoryDate(room.getId(), targetDate)) {
                newInventories.add(RoomInventory.builder()
                        .room(room)
                        .inventoryDate(targetDate)
                        .availableCount(room.getQuantity())
                        .build());
            }
        }
        if (!newInventories.isEmpty()) {
            inventoryRepository.saveAll(newInventories);
            log.info("Đã bổ sung thêm {} bản ghi inventory cho ngày {}", newInventories.size(), targetDate);
        }
    }

    // ============================================================
    // HOST CALENDAR — view + edit
    // ============================================================

    /** Verify property thuộc host hiện tại + return property. */
    private Property requireOwnedProperty(Long propertyId) {
        User host = securityUtils.getCurrentUser();
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.PROPERTY_NOT_FOUND));
        if (!property.getHost().getId().equals(host.getId())) {
            throw new AppException(ErrorCode.NOT_PROPERTY_OWNER);
        }
        return property;
    }

    private Room requireOwnedRoom(Long propertyId, Long roomId) {
        requireOwnedProperty(propertyId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_IS_NOT_FOUND));
        if (!room.getProperty().getId().equals(propertyId)) {
            throw new AppException(ErrorCode.ROOM_NOT_BELONG_TO_PROPERTY);
        }
        return room;
    }

    /**
     * Lấy calendar cho 1 property trong khoảng [from, to] inclusive.
     * Mỗi room → list ngày với availableCount + bookedCount + hasInventory.
     */
    @Transactional(readOnly = true)
    public HostCalendarResponse getCalendarForProperty(Long propertyId, LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (days > MAX_QUERY_RANGE_DAYS) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        requireOwnedProperty(propertyId);
        List<Room> rooms = roomRepository.findByPropertyId(propertyId);

        List<HostCalendarResponse.RoomCalendar> roomCalendars = new ArrayList<>();
        for (Room room : rooms) {
            roomCalendars.add(buildRoomCalendar(room, from, to));
        }

        return HostCalendarResponse.builder()
                .fromDate(from)
                .toDate(to)
                .rooms(roomCalendars)
                .build();
    }

    private HostCalendarResponse.RoomCalendar buildRoomCalendar(Room room, LocalDate from, LocalDate to) {
        // Inventory rows trong range
        List<RoomInventory> invRows = inventoryRepository.findByRoomIdAndInventoryDateBetween(
                room.getId(), from, to);
        Map<LocalDate, RoomInventory> invByDate = new HashMap<>();
        for (RoomInventory ri : invRows) {
            invByDate.put(ri.getInventoryDate(), ri);
        }

        // Bookings overlap [from, to + 1) — to exclusive
        List<Booking> overlapping = bookingRepository.findActiveOverlapping(
                room.getId(), from, to.plusDays(1));

        // Tính bookedCount per date: với mỗi booking, mỗi ngày trong [checkIn, checkOut)
        // mà cũng nằm trong [from, to] thì cộng booking.roomQuantity vào date đó
        Map<LocalDate, Integer> bookedByDate = new HashMap<>();
        for (Booking b : overlapping) {
            LocalDate dayIter = b.getCheckInDate().isBefore(from) ? from : b.getCheckInDate();
            LocalDate dayEndExclusive = b.getCheckOutDate().isAfter(to.plusDays(1))
                    ? to.plusDays(1) : b.getCheckOutDate();
            while (dayIter.isBefore(dayEndExclusive)) {
                bookedByDate.merge(dayIter, b.getRoomQuantity(), Integer::sum);
                dayIter = dayIter.plusDays(1);
            }
        }

        List<HostCalendarResponse.DayInventory> days = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            RoomInventory ri = invByDate.get(d);
            int booked = bookedByDate.getOrDefault(d, 0);
            days.add(HostCalendarResponse.DayInventory.builder()
                    .date(d)
                    .availableCount(ri != null ? ri.getAvailableCount() : 0)
                    .bookedCount(booked)
                    .hasInventory(ri != null)
                    .build());
        }

        return HostCalendarResponse.RoomCalendar.builder()
                .roomId(room.getId())
                .roomType(room.getRoomType())
                .capacity(room.getCapacity())
                .quantity(room.getQuantity())
                .days(days)
                .build();
    }

    /**
     * Bulk update availableCount cho [fromDate, toDate] inclusive của 1 room.
     * Validate: availableCount + bookedCount(d) <= room.quantity với MỌI d.
     * Upsert: tạo row mới nếu chưa có, update nếu đã có.
     */
    @Transactional
    public List<HostCalendarResponse.DayInventory> bulkUpdateInventory(
            Long propertyId, Long roomId, DayInventoryUpdateRequest request) {
        if (request.getFromDate() == null || request.getToDate() == null
                || request.getFromDate().isAfter(request.getToDate())) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        long days = ChronoUnit.DAYS.between(request.getFromDate(), request.getToDate()) + 1;
        if (days > MAX_QUERY_RANGE_DAYS) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        if (request.getAvailableCount() == null || request.getAvailableCount() < 0) {
            throw new AppException(ErrorCode.INVENTORY_EXCEEDS_CAPACITY);
        }

        Room room = requireOwnedRoom(propertyId, roomId);
        if (request.getAvailableCount() > room.getQuantity()) {
            throw new AppException(ErrorCode.INVENTORY_EXCEEDS_CAPACITY);
        }

        // Lock các row đã tồn tại trong range
        List<RoomInventory> locked = inventoryRepository.findAndLockByRoomAndDateRange(
                roomId, request.getFromDate(), request.getToDate());
        Map<LocalDate, RoomInventory> existingByDate = new HashMap<>();
        for (RoomInventory ri : locked) existingByDate.put(ri.getInventoryDate(), ri);

        // Bookings overlap để tính bookedCount/ngày
        List<Booking> overlapping = bookingRepository.findActiveOverlapping(
                roomId, request.getFromDate(), request.getToDate().plusDays(1));
        Map<LocalDate, Integer> bookedByDate = new HashMap<>();
        for (Booking b : overlapping) {
            LocalDate dayIter = b.getCheckInDate().isBefore(request.getFromDate())
                    ? request.getFromDate() : b.getCheckInDate();
            LocalDate dayEndExclusive = b.getCheckOutDate().isAfter(request.getToDate().plusDays(1))
                    ? request.getToDate().plusDays(1) : b.getCheckOutDate();
            while (dayIter.isBefore(dayEndExclusive)) {
                bookedByDate.merge(dayIter, b.getRoomQuantity(), Integer::sum);
                dayIter = dayIter.plusDays(1);
            }
        }

        // Validate: với mỗi date, availableCount + bookedCount <= quantity
        // (đảm bảo không oversell — nếu host đặt available quá cao
        // thì có thể vượt quantity)
        // Đồng thời: availableCount >= 0 (đã check), nhưng ngầm hiểu host KHÔNG
        // được set thấp hơn quantity - bookedCount lúc đó là OVER-SELL không xảy ra.
        // Logic chính: invariant `availableCount + bookedCount <= quantity` luôn đúng.
        for (LocalDate d = request.getFromDate(); !d.isAfter(request.getToDate()); d = d.plusDays(1)) {
            int booked = bookedByDate.getOrDefault(d, 0);
            if (request.getAvailableCount() + booked > room.getQuantity()) {
                throw new AppException(ErrorCode.INVENTORY_EXCEEDS_CAPACITY);
            }
        }

        // Upsert
        List<RoomInventory> toSave = new ArrayList<>();
        for (LocalDate d = request.getFromDate(); !d.isAfter(request.getToDate()); d = d.plusDays(1)) {
            RoomInventory ri = existingByDate.get(d);
            if (ri == null) {
                ri = RoomInventory.builder()
                        .room(room)
                        .inventoryDate(d)
                        .availableCount(request.getAvailableCount())
                        .build();
            } else {
                ri.setAvailableCount(request.getAvailableCount());
            }
            toSave.add(ri);
        }
        inventoryRepository.saveAll(toSave);

        // Build response = the days slice
        List<HostCalendarResponse.DayInventory> result = new ArrayList<>();
        for (RoomInventory ri : toSave) {
            int booked = bookedByDate.getOrDefault(ri.getInventoryDate(), 0);
            result.add(HostCalendarResponse.DayInventory.builder()
                    .date(ri.getInventoryDate())
                    .availableCount(ri.getAvailableCount())
                    .bookedCount(booked)
                    .hasInventory(true)
                    .build());
        }
        log.info("Host updated inventory cho room {} từ {} đến {}, value {}",
                roomId, request.getFromDate(), request.getToDate(), request.getAvailableCount());
        return result;
    }

    /**
     * Mở thêm inventory đến untilDate. Idempotent: skip ngày đã tồn tại.
     * Trả về số lượng row mới tạo và lastDate hiện tại sau khi extend.
     */
    @Transactional
    public ExtendResult extendInventory(Long propertyId, Long roomId, ExtendInventoryRequest request) {
        if (request.getUntilDate() == null) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        LocalDate today = LocalDate.now();
        if (request.getUntilDate().isBefore(today)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        if (ChronoUnit.DAYS.between(today, request.getUntilDate()) > MAX_EXTEND_DAYS_AHEAD) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        Room room = requireOwnedRoom(propertyId, roomId);

        LocalDate maxDate = inventoryRepository.findMaxInventoryDateByRoomId(roomId);
        LocalDate startDate = (maxDate != null) ? maxDate.plusDays(1) : today;
        if (startDate.isBefore(today)) startDate = today;

        if (startDate.isAfter(request.getUntilDate())) {
            return new ExtendResult(0, maxDate != null ? maxDate : today.minusDays(1));
        }

        List<RoomInventory> toCreate = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(request.getUntilDate()); d = d.plusDays(1)) {
            // Idempotent guard: chỉ tạo nếu chưa có (race-safe nếu unique constraint trên (room_id, inventory_date))
            if (!inventoryRepository.existsByRoomIdAndInventoryDate(roomId, d)) {
                toCreate.add(RoomInventory.builder()
                        .room(room)
                        .inventoryDate(d)
                        .availableCount(room.getQuantity())
                        .build());
            }
        }
        if (!toCreate.isEmpty()) {
            inventoryRepository.saveAll(toCreate);
        }

        log.info("Host mở thêm {} ngày inventory cho room {} đến {}",
                toCreate.size(), roomId, request.getUntilDate());
        return new ExtendResult(toCreate.size(), request.getUntilDate());
    }

    public record ExtendResult(int created, LocalDate lastDate) {}
}
