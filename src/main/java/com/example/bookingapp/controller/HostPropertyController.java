package com.example.bookingapp.controller;

import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.dto.PropertyDetailResponse;
import com.example.bookingapp.entity.Property;
import com.example.bookingapp.entity.PropertyImage;
import com.example.bookingapp.entity.Room;
import com.example.bookingapp.form.PropertyRequest;
import com.example.bookingapp.form.RoomRequest;
import com.example.bookingapp.service.PropertyImageService;
import com.example.bookingapp.service.PropertyService;
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

    /** Soft delete: ẩn homestay khỏi tìm kiếm public, dữ liệu liên quan được giữ. */
    @PatchMapping("/{id}/deactivate")
    public ApiResponse<Property> deactivate(@PathVariable Long id) {
        return ApiResponse.success(propertyService.deactivateMyProperty(id));
    }

    @PatchMapping("/{id}/activate")
    public ApiResponse<Property> activate(@PathVariable Long id) {
        return ApiResponse.success(propertyService.activateMyProperty(id));
    }

    /** Hard delete: xoá vĩnh viễn (chỉ thành công khi không còn ràng buộc khoá ngoại). */
    @DeleteMapping("/{id}")
    public ApiResponse<String> hardDelete(@PathVariable Long id) {
        propertyService.deleteMyProperty(id);
        return ApiResponse.success("Đã xoá homestay vĩnh viễn");
    }

    @PostMapping("/{id}/rooms")
    public ApiResponse<Room> addRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request) {
        return ApiResponse.success(propertyService.addRoomToProperty(id, request));
    }

    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public ApiResponse<List<Map<String, Object>>> uploadImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }
        List<PropertyImage> uploaded = propertyImageService.uploadImagesForProperty(id, files);
        List<Map<String, Object>> data = uploaded.stream()
                .map(img -> Map.<String, Object>of(
                        "id", img.getId(),
                        "imageUrl", img.getImageUrl(),
                        "isThumbnail", img.getIsThumbnail()))
                .toList();
        return ApiResponse.success(data);
    }
}
