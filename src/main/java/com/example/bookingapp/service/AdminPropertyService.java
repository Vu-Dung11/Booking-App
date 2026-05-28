package com.example.bookingapp.service;

import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.dto.admin.AdminPropertyDetailResponse;
import com.example.bookingapp.dto.admin.AdminPropertyResponse;
import com.example.bookingapp.entity.Property;
import com.example.bookingapp.entity.PropertyImage;
import com.example.bookingapp.entity.Review;
import com.example.bookingapp.entity.Room;
import com.example.bookingapp.entity.RoomImage;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.PropertyImageRepository;
import com.example.bookingapp.repository.PropertyRepository;
import com.example.bookingapp.repository.ReviewRepository;
import com.example.bookingapp.repository.RoomImageRepository;
import com.example.bookingapp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPropertyService {

    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final RoomImageRepository roomImageRepository;
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public Page<AdminPropertyResponse> search(String city, Long hostId, Boolean isActive,
                                              String keyword, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String cityFilter = (city == null || city.isBlank()) ? null : city.trim();
        return propertyRepository.searchForAdmin(cityFilter, hostId, isActive, kw, pageable)
                .map(this::toListResponse);
    }

    private AdminPropertyResponse toListResponse(Property p) {
        AdminPropertyResponse res = AdminPropertyResponse.fromEntity(p);
        propertyImageRepository.findFirstByPropertyIdAndIsThumbnailTrue(p.getId())
                .ifPresent(img -> res.setThumbnailUrl(img.getImageUrl()));
        List<Review> reviews = reviewRepository.findByPropertyId(p.getId());
        if (reviews.isEmpty()) {
            res.setAverageRating(0.0);
            res.setReviewCount(0L);
        } else {
            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
            res.setAverageRating(avg);
            res.setReviewCount((long) reviews.size());
        }
        res.setRoomCount((long) roomRepository.findByPropertyId(p.getId()).size());
        return res;
    }

    public AdminPropertyDetailResponse getDetail(Long id) {
        Property p = propertyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROPERTY_NOT_FOUND));

        // Images
        List<String> imageUrls = propertyImageRepository.findByPropertyId(p.getId()).stream()
                .map(PropertyImage::getImageUrl)
                .toList();

        // Rooms + room thumbnail
        List<Room> rooms = roomRepository.findByPropertyId(p.getId());
        List<AdminPropertyDetailResponse.RoomSummary> roomSummaries = rooms.stream()
                .map(r -> {
                    String thumb = roomImageRepository.findFirstByRoomIdAndIsThumbnailTrue(r.getId())
                            .map(RoomImage::getImageUrl)
                            .orElse(null);
                    return AdminPropertyDetailResponse.RoomSummary.builder()
                            .id(r.getId())
                            .roomType(r.getRoomType())
                            .capacity(r.getCapacity())
                            .basePrice(r.getBasePrice())
                            .quantity(r.getQuantity())
                            .thumbnailUrl(thumb)
                            .build();
                })
                .toList();

        // Reviews aggregate
        List<Review> reviews = reviewRepository.findByPropertyId(p.getId());
        double avg = reviews.isEmpty() ? 0
                : reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        long totalReviews = reviews.size();

        // Bookings aggregate
        long totalBookings = bookingRepository.findByRoom_Property_Id(p.getId(),
                org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();

        return AdminPropertyDetailResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .address(p.getAddress())
                .city(p.getCity())
                .country(p.getCountry())
                .isActive(p.getIsActive())
                .hostId(p.getHost() != null ? p.getHost().getId() : null)
                .hostEmail(p.getHost() != null ? p.getHost().getEmail() : null)
                .hostFullName(p.getHost() != null ? p.getHost().getFullName() : null)
                .hostPhone(p.getHost() != null ? p.getHost().getPhoneNumber() : null)
                .images(imageUrls)
                .rooms(roomSummaries)
                .totalReviews(totalReviews)
                .averageRating(avg)
                .totalBookings(totalBookings)
                .build();
    }

    @Transactional
    public AdminPropertyResponse toggleActive(Long id) {
        Property p = propertyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROPERTY_NOT_FOUND));
        p.setIsActive(!Boolean.TRUE.equals(p.getIsActive()));
        propertyRepository.save(p);
        return toListResponse(p);
    }

    /**
     * Soft delete: set isActive=false. Giữ nguyên booking/review history.
     * Idempotent — gọi nhiều lần không lỗi.
     */
    @Transactional
    public void softDelete(Long id) {
        Property p = propertyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROPERTY_NOT_FOUND));
        p.setIsActive(false);
        propertyRepository.save(p);
    }
}
