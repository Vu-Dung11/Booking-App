package com.example.bookingapp.controller;

import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.entity.PropertyImage;
import com.example.bookingapp.service.PropertyImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyImageController {

    private final PropertyImageService propertyImageService;

    @PostMapping(value = "/{propertyId}/images", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('HOST')")
    public ApiResponse<List<Map<String, Object>>> uploadPropertyImages(
            @PathVariable Long propertyId,
            @RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        List<PropertyImage> uploadedImages = propertyImageService.uploadImagesForProperty(propertyId, files);

        List<Map<String, Object>> responseData = uploadedImages.stream()
                .map(img -> Map.<String, Object>of(
                        "id", img.getId(),
                        "imageUrl", img.getImageUrl(),
                        "isThumbnail", img.getIsThumbnail()
                ))
                .toList();

        return ApiResponse.success(responseData);

    }
}