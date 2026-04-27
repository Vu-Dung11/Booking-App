package com.example.bookingapp.service;

import com.example.bookingapp.dto.PropertyDetailResponse;
import com.example.bookingapp.dto.PropertySearchResponse;
import com.example.bookingapp.dto.RoomSearchResponse;
import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.entity.Property;
import com.example.bookingapp.entity.Room;
import com.example.bookingapp.entity.User;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.form.PropertyRequest;
import com.example.bookingapp.form.RoomRequest;
import com.example.bookingapp.form.SearchRequest;
import com.example.bookingapp.repository.PropertyRepository;
import com.example.bookingapp.repository.RoomRepository;
import com.example.bookingapp.configuration.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final InventoryService inventoryService;
    private final SecurityUtils securityUtils;



    @Transactional
    public Property createProperty(PropertyRequest request) {
        User host = securityUtils.getCurrentUser();

        Property property = Property.builder()
                .host(host)
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .isActive(true)
                .build();

        return propertyRepository.save(property);
    }

    // thêm phòng vào property
    @Transactional
    public Room addRoomToProperty(Long propertyId, RoomRequest roomRequest) {
        User host = securityUtils.getCurrentUser();
        // Tìm property và kiểm tra xem property này có thuộc về host đang đăng nhập
        // không
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.PROPERTY_NOT_FOUND));

        if (!property.getHost().getId().equals(host.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Room room = Room.builder()
                .property(property)
                .roomType(roomRequest.getRoomType())
                .capacity(roomRequest.getCapacity())
                .basePrice(roomRequest.getBasePrice())
                .quantity(roomRequest.getQuantity())
                .build();
        Room savedRoom = roomRepository.save(room);
        inventoryService.generateInitialInventory(savedRoom);
        return savedRoom;
    }

    @Transactional(readOnly = true)
    public List<PropertySearchResponse> searchProperties(SearchRequest request) {
        // 1. Tính số đêm lưu trú
        long duration = java.time.temporal.ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());

        // 2. Gọi Repository lấy danh sách Homestay thỏa mãn
        List<Property> properties = propertyRepository.searchAvailableProperties(
                request.getCity(),
                request.getCheckIn(),
                request.getCheckOut(),
                request.getGuests(),
                duration);

        // 3. Map dữ liệu sang DTO (Ở dự án thực tế nên dùng MapStruct để code sạch hơn)
        return properties.stream().map(p -> {
            // Lọc lại các phòng thực sự còn trống của property này
            List<RoomSearchResponse> rooms = roomRepository.findByPropertyId(p.getId()).stream()
                    .filter(r -> r.getCapacity() >= request.getGuests())
                    // Lưu ý: Chỗ này cần gọi thêm một hàm check inventory của riêng phòng này
                    // để đảm bảo tính chính xác trước khi trả về
                    .map(r -> RoomSearchResponse.builder()
                            .roomId(r.getId())
                            .roomType(r.getRoomType())
                            .price(r.getBasePrice())
                            .capacity(r.getCapacity())
                            .build())
                    .toList();

            return PropertySearchResponse.builder()
                    .propertyId(p.getId())
                    .propertyName(p.getName())
                    .address(p.getAddress())
                    .city(p.getCity())
                    .availableRooms(rooms)
                    .minPrice(rooms.stream().map(RoomSearchResponse::getPrice)
                            .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO))
                    .build();
        }).toList();
    }
    @Transactional(readOnly = true)
    public Page<Property> getAllProperties(Pageable pageable) {
        return propertyRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Property getPropertyById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROPERTY_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PropertyDetailResponse getPropertyDetail(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROPERTY_NOT_FOUND));

        List<RoomSearchResponse> rooms = roomRepository.findByPropertyId(id).stream()
                .map(r -> RoomSearchResponse.builder()
                        .roomId(r.getId())
                        .roomType(r.getRoomType())
                        .price(r.getBasePrice())
                        .capacity(r.getCapacity())
                        .build())
                .toList();

        return PropertyDetailResponse.builder()
                .propetyId(property.getId())
                .name(property.getName())
                .description(property.getDescription())
                .address(property.getAddress())
                .city(property.getCity())
                .country(property.getCountry())
                .rooms(rooms)
                .build();
    }
}
