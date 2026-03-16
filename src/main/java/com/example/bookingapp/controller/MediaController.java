package com.example.bookingapp.controller;

import com.example.bookingapp.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final CloudinaryService cloudinaryService;

    // API Upload ảnh (Chú ý: consumes = "multipart/form-data")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Vui lòng chọn một file ảnh!");
            }

            Map<String, Object> uploadResult = cloudinaryService.uploadFile(file, "test_uploads");

            String imageUrl = uploadResult.get("secure_url").toString();
            String publicId = uploadResult.get("public_id").toString();

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Upload ảnh thành công!",
                    "imageUrl", imageUrl, // Đem link này hiện lên thẻ <img> của HTML
                    "publicId", publicId  // Đem mã này lưu vào Database
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi upload: " + e.getMessage());
        }
    }
}