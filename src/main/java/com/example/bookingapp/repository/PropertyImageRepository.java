package com.example.bookingapp.repository;


import com.example.bookingapp.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {

    // Lấy toàn bộ ảnh của một Homestay
    List<PropertyImage> findByPropertyId(Long propertyId);

    // Kiểm tra xem Homestay đã có ảnh Thumbnail chưa
    boolean existsByPropertyIdAndIsThumbnailTrue(Long propertyId);

    // Tìm ảnh thumbnail hiện tại của property (nếu có)
    java.util.Optional<PropertyImage> findFirstByPropertyIdAndIsThumbnailTrue(Long propertyId);
}