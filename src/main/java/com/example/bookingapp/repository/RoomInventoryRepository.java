package com.example.bookingapp.repository;

import com.example.bookingapp.entity.RoomInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {


    boolean existsByRoomIdAndInventoryDate(Long roomId, LocalDate date);
}
