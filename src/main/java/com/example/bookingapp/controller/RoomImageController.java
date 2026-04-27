package com.example.bookingapp.controller;

import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.entity.RoomImage;
import com.example.bookingapp.service.RoomImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomImageController {

    private final RoomImageService roomImageService;

    @PostMapping(value = "/{roomId}/images", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('HOST')")
    public ApiResponse<List<Map<String, Object>>> uploadRoomImages(
            @PathVariable Long roomId,
            @RequestParam("files") List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        List<RoomImage> uploadedImages = roomImageService.uploadImagesForRoom(roomId, files);
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
