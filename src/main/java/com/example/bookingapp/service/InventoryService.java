package com.example.bookingapp.service;


import com.example.bookingapp.entity.Room;
import com.example.bookingapp.entity.RoomInventory;
import com.example.bookingapp.repository.RoomInventoryRepository;
import com.example.bookingapp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final RoomInventoryRepository inventoryRepository;
    private final RoomRepository roomRepository;

    private static final int DAYS_IN_ADVANCE = 90; // Mở lịch trước 90 ngày

    /**
     * Nghiệp vụ 1: Sinh kho phòng lần đầu tiên ngay khi Host tạo phòng mới
     */
    @Transactional
    public void generateInitialInventory(Room room) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(DAYS_IN_ADVANCE);

        List<RoomInventory> inventories = new ArrayList<>();

        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            RoomInventory inventory = RoomInventory.builder()
                    .room(room)
                    .inventoryDate(date)
                    .availableCount(room.getQuantity()) // Mặc định số phòng trống = tổng số phòng
                    .build();
            inventories.add(inventory);
        }

        // Lưu toàn bộ 90 bản ghi vào database cùng lúc (Batch Insert) để tối ưu hiệu suất
        inventoryRepository.saveAll(inventories);
        log.info("Đã tạo thành công lịch 90 ngày cho phòng ID: {}", room.getId());
    }

    /**
     * Nghiệp vụ 2: Cron Job chạy tự động lúc 00:00 mỗi ngày để bảo trì kho phòng.
     * Nó sẽ quét tất cả các phòng và tạo thêm lịch cho ngày thứ 91.
     */
    @Scheduled(cron = "0 0 0 * * ?") // Cú pháp Cron: Chạy vào lúc 0h 0p 0s mỗi ngày
    @Transactional
    public void maintainDailyInventory() {
        log.info("Bắt đầu chạy Job bảo trì kho phòng hàng ngày...");

        LocalDate targetDate = LocalDate.now().plusDays(DAYS_IN_ADVANCE - 1); // Ngày thứ 90 tính từ hôm nay
        List<Room> allRooms = roomRepository.findAll();
        List<RoomInventory> newInventories = new ArrayList<>();

        for (Room room : allRooms) {
            // Check xem ngày đó đã có lịch chưa, nếu chưa thì mới tạo
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
}
