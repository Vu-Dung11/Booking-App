package com.example.bookingapp.controller;

import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.dto.PropertyDetailResponse;
import com.example.bookingapp.entity.Property;
import com.example.bookingapp.entity.PropertyImage;
import com.example.bookingapp.entity.Room;
import com.example.bookingapp.entity.RoomImage;
import com.example.bookingapp.form.PropertyRequest;
import com.example.bookingapp.form.RoomRequest;
import com.example.bookingapp.service.PropertyImageService;
import com.example.bookingapp.service.PropertyService;
import com.example.bookingapp.service.RoomImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Endpoints quản lý homestay dành riêng cho HOST.
 * Toàn bộ thao tác chỉ áp dụng trên các property thuộc về host hiện tại.
 */
@RestController
@RequestMapping("/api/v1/host/properties")
@PreAuthorize("hasRole('HOST')")
@RequiredArgsConstructor
public class HostPropertyController {

    private final PropertyService propertyService;
    private final PropertyImageService propertyImageService;
    private final RoomImageService roomImageService;

    // ===================== PROPERTY =====================

    @GetMapping
    public ApiResponse<Page<Property>> listMyProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(propertyService.getMyProperties(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<Property> getById(@PathVariable Long id) {
        return ApiResponse.success(propertyService.getMyPropertyById(id));
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<PropertyDetailResponse> getDetail(@PathVariable Long id) {
        return ApiResponse.success(propertyService.getMyPropertyDetail(id));
    }

    @PostMapping
    public ApiResponse<Property> create(@Valid @RequestBody PropertyRequest request) {
        return ApiResponse.success(propertyService.createProperty(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Property> update(
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequest request) {
        return ApiResponse.success(propertyService.updateMyProperty(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public ApiResponse<Property> deactivate(@PathVariable Long id) {
        return ApiResponse.success(propertyService.deactivateMyProperty(id));
    }

    @PatchMapping("/{id}/activate")
    public ApiResponse<Property> activate(@PathVariable Long id) {
        return ApiResponse.success(propertyService.activateMyProperty(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> hardDelete(@PathVariable Long id) {
        propertyService.deleteMyProperty(id);
        return ApiResponse.success("Đã xoá homestay vĩnh viễn");
    }

    // ===================== ROOMS =====================

    @PostMapping("/{id}/rooms")
    public ApiResponse<Room> addRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request) {
        return ApiResponse.success(propertyService.addRoomToProperty(id, request));
    }

    @PutMapping("/{pid}/rooms/{rid}")
    public ApiResponse<Room> updateRoom(
            @PathVariable Long pid,
            @PathVariable Long rid,
            @Valid @RequestBody RoomRequest request) {
        return ApiResponse.success(propertyService.updateMyRoom(pid, rid, request));
    }

    @DeleteMapping("/{pid}/rooms/{rid}")
    public ApiResponse<String> deleteRoom(
            @PathVariable Long pid,
            @PathVariable Long rid) {
        propertyService.deleteMyRoom(pid, rid);
        return ApiResponse.success("Đã xoá phòng");
    }

    // ===================== PROPERTY IMAGES =====================

    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public ApiResponse<List<Map<String, Object>>> uploadImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }
        List<PropertyImage> uploaded = propertyImageService.uploadImagesForProperty(id, files);
        return ApiResponse.success(toPropertyImageData(uploaded));
    }

    @GetMapping("/{id}/images")
    public ApiResponse<List<Map<String, Object>>> listImages(@PathVariable Long id) {
        return ApiResponse.success(toPropertyImageData(propertyImageService.listImagesForProperty(id)));
    }

    @DeleteMapping("/{pid}/images/{imgId}")
    public ApiResponse<String> deletePropertyImage(
            @PathVariable Long pid,
            @PathVariable Long imgId) {
        propertyImageService.deleteImage(pid, imgId);
        return ApiResponse.success("Đã xoá ảnh");
    }

    @PatchMapping("/{pid}/images/{imgId}/thumbnail")
    public ApiResponse<Map<String, Object>> setPropertyThumbnail(
            @PathVariable Long pid,
            @PathVariable Long imgId) {
        PropertyImage img = propertyImageService.setThumbnail(pid, imgId);
        return ApiResponse.success(toPropertyImageMap(img));
    }

    // ===================== ROOM IMAGES =====================

    @PostMapping(value = "/{pid}/rooms/{rid}/images", consumes = "multipart/form-data")
    public ApiResponse<List<Map<String, Object>>> uploadRoomImages(
            @PathVariable Long pid,
            @PathVariable Long rid,
            @RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }
        List<RoomImage> uploaded = roomImageService.uploadImagesForRoom(pid, rid, files);
        return ApiResponse.success(toRoomImageData(uploaded));
    }

    @GetMapping("/{pid}/rooms/{rid}/images")
    public ApiResponse<List<Map<String, Object>>> listRoomImages(
            @PathVariable Long pid,
            @PathVariable Long rid) {
        return ApiResponse.success(toRoomImageData(roomImageService.listImagesForRoom(pid, rid)));
    }

    @DeleteMapping("/{pid}/rooms/{rid}/images/{imgId}")
    public ApiResponse<String> deleteRoomImage(
            @PathVariable Long pid,
            @PathVariable Long rid,
            @PathVariable Long imgId) {
        roomImageService.deleteImage(pid, rid, imgId);
        return ApiResponse.success("Đã xoá ảnh");
    }

    @PatchMapping("/{pid}/rooms/{rid}/images/{imgId}/thumbnail")
    public ApiResponse<Map<String, Object>> setRoomThumbnail(
            @PathVariable Long pid,
            @PathVariable Long rid,
            @PathVariable Long imgId) {
        RoomImage img = roomImageService.setThumbnail(pid, rid, imgId);
        return ApiResponse.success(toRoomImageMap(img));
    }

    // ===================== mappers =====================

    private List<Map<String, Object>> toPropertyImageData(List<PropertyImage> images) {
        return images.stream().map(this::toPropertyImageMap).toList();
    }

    private Map<String, Object> toPropertyImageMap(PropertyImage img) {
        return Map.of(
                "id", img.getId(),
                "imageUrl", img.getImageUrl(),
                "isThumbnail", Boolean.TRUE.equals(img.getIsThumbnail())
        );
    }

    private List<Map<String, Object>> toRoomImageData(List<RoomImage> images) {
        return images.stream().map(this::toRoomImageMap).toList();
    }

    private Map<String, Object> toRoomImageMap(RoomImage img) {
        return Map.of(
                "id", img.getId(),
                "imageUrl", img.getImageUrl(),
                "isThumbnail", Boolean.TRUE.equals(img.getIsThumbnail())
        );
    }
}
