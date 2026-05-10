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
import java.util.Optional;

@Repository
public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {
    boolean existsByRoomIdAndInventoryDate(Long roomId, LocalDate date);
    void deleteByRoomId(Long roomId);

    Optional<RoomInventory> findByRoomIdAndInventoryDate(Long roomId, LocalDate date);

    List<RoomInventory> findByRoomIdAndInventoryDateBetween(Long roomId, LocalDate from, LocalDate to);

    @Query("SELECT MAX(ri.inventoryDate) FROM RoomInventory ri WHERE ri.room.id = :roomId")
    LocalDate findMaxInventoryDateByRoomId(@Param("roomId") Long roomId);

    // KỸ THUẬT KHÓA BI QUAN (SELECT ... FOR UPDATE)
    // Khi 1 transaction đang gọi hàm này, các transaction khác phải đứng chờ
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ri FROM RoomInventory ri WHERE ri.room.id = :roomId AND ri.inventoryDate >= :checkIn AND ri.inventoryDate < :checkOut")
    List<RoomInventory> findAndLockInventoryByRoomAndDates(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    /** Lock các inventory rows trong khoảng [from, to] inclusive cho host edit. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ri FROM RoomInventory ri WHERE ri.room.id = :roomId AND ri.inventoryDate BETWEEN :from AND :to")
    List<RoomInventory> findAndLockByRoomAndDateRange(
            @Param("roomId") Long roomId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
