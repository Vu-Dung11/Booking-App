package com.example.bookingapp.repository;

import com.example.bookingapp.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {

    // Lấy toàn bộ ảnh của một phòng cụ thể
    List<RoomImage> findByRoomId(Long roomId);

    // Kiểm tra xem phòng này đã có ảnh Thumbnail (ảnh đại diện) chưa
    boolean existsByRoomIdAndIsThumbnailTrue(Long roomId);

    // Tìm ảnh thumbnail hiện tại của room (nếu có)
    java.util.Optional<RoomImage> findFirstByRoomIdAndIsThumbnailTrue(Long roomId);

    void deleteByRoomId(Long roomId);
}