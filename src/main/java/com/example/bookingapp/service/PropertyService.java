package com.example.bookingapp.service;

import com.example.bookingapp.dto.PropertyDetailResponse;
import com.example.bookingapp.dto.PropertySearchResponse;
import com.example.bookingapp.dto.RoomSearchResponse;
import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.entity.Property;
import com.example.bookingapp.entity.PropertyImage;
import com.example.bookingapp.entity.Room;
import com.example.bookingapp.entity.RoomImage;
import com.example.bookingapp.entity.User;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.form.PropertyRequest;
import com.example.bookingapp.form.RoomRequest;
import com.example.bookingapp.form.SearchRequest;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.PropertyImageRepository;
import com.example.bookingapp.repository.PropertyRepository;
import com.example.bookingapp.repository.RoomImageRepository;
import com.example.bookingapp.repository.RoomInventoryRepository;
import com.example.bookingapp.repository.RoomRepository;
import com.example.bookingapp.configuration.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PropertyService {
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final InventoryService inventoryService;
    private final SecurityUtils securityUtils;
    private final PropertyImageRepository propertyImageRepository;
    private final RoomImageRepository roomImageRepository;
    private final BookingRepository bookingRepository;
    private final RoomInventoryRepository roomInventoryRepository;
    private final CloudinaryService cloudinaryService;

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

    @Transactional
    public Room addRoomToProperty(Long propertyId, RoomRequest roomRequest) {
        Property property = requireOwnedProperty(propertyId);
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
        long duration = java.time.temporal.ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        List<Property> properties = propertyRepository.searchAvailableProperties(
                request.getCity(),
                request.getCheckIn(),
                request.getCheckOut(),
                request.getGuests(),
                duration);

        return properties.stream().map(p -> {
            List<RoomSearchResponse> rooms = roomRepository.findByPropertyId(p.getId()).stream()
                    .filter(r -> r.getCapacity() >= request.getGuests())
                    .map(this::toRoomSearchResponse)
                    .toList();

            String thumb = propertyImageRepository.findFirstByPropertyIdAndIsThumbnailTrue(p.getId())
                    .map(PropertyImage::getImageUrl)
                    .orElseGet(() -> rooms.stream()
                            .map(RoomSearchResponse::getThumbnailUrl)
                            .filter(java.util.Objects::nonNull)
                            .findFirst().orElse(null));

            return PropertySearchResponse.builder()
                    .propertyId(p.getId())
                    .propertyName(p.getName())
                    .address(p.getAddress())
                    .city(p.getCity())
                    .thumbnailUrl(thumb)
                    .availableRooms(rooms)
                    .minPrice(rooms.stream().map(RoomSearchResponse::getPrice)
                            .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO))
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctCities() {
        return propertyRepository.findDistinctActiveCities();
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
        return buildDetail(property);
    }

    /**
     * Detail có lọc phòng theo khoảng ngày + sức chứa.
     *  - checkIn/checkOut bắt buộc đi cùng nhau; nếu null → fallback {@link #getPropertyDetail(Long)}.
     *  - guests null/0 → không lọc theo capacity.
     *  - Mỗi room trả về kèm {@code availableCount} = min(availableCount) qua các đêm.
     *  - Lọc bỏ room có availableCount = 0.
     */
    @Transactional(readOnly = true)
    public PropertyDetailResponse getPropertyDetailWithAvailability(
            Long id, LocalDate checkIn, LocalDate checkOut, Integer guests) {

        if (checkIn == null || checkOut == null) {
            return getPropertyDetail(id);
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new AppException(ErrorCode.CHECK_OUT_MUST_BE_AFTER_CHECK_IN);
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROPERTY_NOT_FOUND));

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        Optional<PropertyImage> propThumb = propertyImageRepository.findFirstByPropertyIdAndIsThumbnailTrue(id);
        String propertyThumb = propThumb.map(PropertyImage::getImageUrl).orElse(null);

        List<RoomSearchResponse> rooms = roomRepository.findByPropertyId(id).stream()
                .filter(r -> guests == null || guests <= 0 || r.getCapacity() >= guests)
                .map(r -> toRoomSearchResponseWithAvailability(r, checkIn, checkOut, nights))
                .filter(r -> r.getAvailableCount() != null && r.getAvailableCount() > 0)
                .toList();

        return PropertyDetailResponse.builder()
                .propetyId(id)
                .name(property.getName())
                .description(property.getDescription())
                .address(property.getAddress())
                .city(property.getCity())
                .country(property.getCountry())
                .isActive(property.getIsActive())
                .thumbnailUrl(propertyThumb)
                .rooms(rooms)
                .build();
    }

    /** Build room kèm minAvailable trong [checkIn, checkOut). */
    private RoomSearchResponse toRoomSearchResponseWithAvailability(
            Room r, LocalDate checkIn, LocalDate checkOut, long nights) {
        // BETWEEN trong JPA là inclusive 2 đầu → dùng checkOut.minusDays(1) để bao đêm
        // cuối cùng của booking (đêm checkOut-1 → sáng checkOut khách trả phòng).
        List<com.example.bookingapp.entity.RoomInventory> invs = roomInventoryRepository
                .findByRoomIdAndInventoryDateBetween(r.getId(), checkIn, checkOut.minusDays(1));

        Integer minAvailable;
        if (invs.size() < nights) {
            // Có đêm chưa được host mở inventory → coi như hết phòng
            minAvailable = 0;
        } else {
            minAvailable = invs.stream()
                    .mapToInt(com.example.bookingapp.entity.RoomInventory::getAvailableCount)
                    .min()
                    .orElse(0);
        }

        Optional<RoomImage> thumb = roomImageRepository.findFirstByRoomIdAndIsThumbnailTrue(r.getId());
        return RoomSearchResponse.builder()
                .roomId(r.getId())
                .roomType(r.getRoomType())
                .price(r.getBasePrice())
                .capacity(r.getCapacity())
                .quantity(r.getQuantity())
                .thumbnailUrl(thumb.map(RoomImage::getImageUrl).orElse(null))
                .availableCount(minAvailable)
                .build();
    }

    // ============================================================
    // HOST-SCOPED OPERATIONS
    // ============================================================

    /** Trả về Property nếu thuộc về host hiện tại, nếu không thì ném NOT_PROPERTY_OWNER. */
    private Property requireOwnedProperty(Long propertyId) {
        User host = securityUtils.getCurrentUser();
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.PROPERTY_NOT_FOUND));
        if (!property.getHost().getId().equals(host.getId())) {
            throw new AppException(ErrorCode.NOT_PROPERTY_OWNER);
        }
        return property;
    }

    @Transactional(readOnly = true)
    public Page<Property> getMyProperties(Pageable pageable) {
        User host = securityUtils.getCurrentUser();
        Page<Property> page = propertyRepository.findByHostId(host.getId(), pageable);
        page.forEach(this::attachThumbnail);
        return page;
    }

    private void attachThumbnail(Property p) {
        propertyImageRepository.findFirstByPropertyIdAndIsThumbnailTrue(p.getId())
                .ifPresent(img -> p.setThumbnailUrl(img.getImageUrl()));
    }

    @Transactional(readOnly = true)
    public Property getMyPropertyById(Long id) {
        return requireOwnedProperty(id);
    }

    @Transactional(readOnly = true)
    public PropertyDetailResponse getMyPropertyDetail(Long id) {
        Property property = requireOwnedProperty(id);
        return buildDetail(property);
    }

    @Transactional
    public Property updateMyProperty(Long id, PropertyRequest request) {
        Property property = requireOwnedProperty(id);
        property.setName(request.getName());
        property.setDescription(request.getDescription());
        property.setAddress(request.getAddress());
        property.setCity(request.getCity());
        property.setCountry(request.getCountry());
        return propertyRepository.save(property);
    }

    @Transactional
    public Property deactivateMyProperty(Long id) {
        Property property = requireOwnedProperty(id);
        property.setIsActive(false);
        return propertyRepository.save(property);
    }

    @Transactional
    public Property activateMyProperty(Long id) {
        Property property = requireOwnedProperty(id);
        property.setIsActive(true);
        return propertyRepository.save(property);
    }

    @Transactional
    public void deleteMyProperty(Long id) {
        Property property = requireOwnedProperty(id);
        propertyRepository.delete(property);
    }

    // ===== Room CRUD =====

    /** Lấy room thuộc property thuộc host hiện tại. */
    private Room requireOwnedRoom(Long propertyId, Long roomId) {
        requireOwnedProperty(propertyId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_IS_NOT_FOUND));
        if (!room.getProperty().getId().equals(propertyId)) {
            throw new AppException(ErrorCode.ROOM_NOT_BELONG_TO_PROPERTY);
        }
        return room;
    }

    @Transactional
    public Room updateMyRoom(Long propertyId, Long roomId, RoomRequest request) {
        Room room = requireOwnedRoom(propertyId, roomId);
        room.setRoomType(request.getRoomType());
        room.setCapacity(request.getCapacity());
        room.setBasePrice(request.getBasePrice());
        room.setQuantity(request.getQuantity());
        return roomRepository.save(room);
    }

    /**
     * Hard delete room. Block nếu còn booking ở bất kỳ status nào.
     * Xoá room_inventory + room_images (kèm Cloudinary) trước, rồi xoá room.
     */
    @Transactional
    public void deleteMyRoom(Long propertyId, Long roomId) {
        Room room = requireOwnedRoom(propertyId, roomId);

        if (bookingRepository.existsByRoom_Id(roomId)) {
            throw new AppException(ErrorCode.ROOM_HAS_BOOKING);
        }

        // Xoá room_inventory trước (FK)
        roomInventoryRepository.deleteByRoomId(roomId);

        // Xoá room_images: Cloudinary trước, rồi DB
        List<RoomImage> images = roomImageRepository.findByRoomId(roomId);
        for (RoomImage img : images) {
            cloudinaryService.deleteFile(img.getPublicId());
        }
        roomImageRepository.deleteByRoomId(roomId);

        roomRepository.delete(room);
    }

    // ============================================================
    // helpers
    // ============================================================

    private PropertyDetailResponse buildDetail(Property property) {
        Long pid = property.getId();
        Optional<PropertyImage> propThumb = propertyImageRepository.findFirstByPropertyIdAndIsThumbnailTrue(pid);
        String propertyThumb = propThumb.map(PropertyImage::getImageUrl).orElse(null);

        List<RoomSearchResponse> rooms = roomRepository.findByPropertyId(pid).stream()
                .map(this::toRoomSearchResponse)
                .toList();

        return PropertyDetailResponse.builder()
                .propetyId(pid)
                .name(property.getName())
                .description(property.getDescription())
                .address(property.getAddress())
                .city(property.getCity())
                .country(property.getCountry())
                .isActive(property.getIsActive())
                .thumbnailUrl(propertyThumb)
                .rooms(rooms)
                .build();
    }

    private RoomSearchResponse toRoomSearchResponse(Room r) {
        Optional<RoomImage> thumb = roomImageRepository.findFirstByRoomIdAndIsThumbnailTrue(r.getId());
        return RoomSearchResponse.builder()
                .roomId(r.getId())
                .roomType(r.getRoomType())
                .price(r.getBasePrice())
                .capacity(r.getCapacity())
                .quantity(r.getQuantity())
                .thumbnailUrl(thumb.map(RoomImage::getImageUrl).orElse(null))
                .build();
    }
}
