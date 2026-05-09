package com.example.bookingapp.repository;

import com.example.bookingapp.entity.RoomInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {
    boolean existsByRoomIdAndInventoryDate(Long roomId, LocalDate date);
    void deleteByRoomId(Long roomId);
    // KỸ THUẬT KHÓA BI QUAN (SELECT ... FOR UPDATE)
    // Khi 1 transaction đang gọi hàm này, các transaction khác phải đứng chờ
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ri FROM RoomInventory ri WHERE ri.room.id = :roomId AND ri.inventoryDate >= :checkIn AND ri.inventoryDate < :checkOut")
    List<RoomInventory> findAndLockInventoryByRoomAndDates(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}
