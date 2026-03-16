package com.example.bookingapp.service;

import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.configuration.utils.SecurityUtils;
import com.example.bookingapp.entity.Property;
import com.example.bookingapp.entity.PropertyImage;
import com.example.bookingapp.entity.User;
import com.example.bookingapp.repository.PropertyImageRepository;
import com.example.bookingapp.repository.PropertyRepository;
import com.example.bookingapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyImageService {

    private final PropertyImageRepository propertyImageRepository;
    private final PropertyRepository propertyRepository;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;


    @Transactional
    public List<PropertyImage> uploadImagesForProperty(Long propertyId, List<MultipartFile> files) {
        User currentHost = securityUtils.getCurrentUser();

        // 1. Tìm Homestay và kiểm tra quyền sở hữu
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->  new AppException(ErrorCode.PROPERTY_NOT_FOUND));

        if (!property.getHost().getId().equals(currentHost.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 2. Kiểm tra xem Homestay đã có ảnh Thumbnail chưa
        boolean hasThumbnail = propertyImageRepository.existsByPropertyIdAndIsThumbnailTrue(propertyId);

        List<PropertyImage> savedImages = new ArrayList<>();

        // 3. Duyệt qua từng file ảnh để upload
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            try {
                // Đẩy lên Cloudinary, lưu vào thư mục "homestay_images"
                Map<String, Object> uploadResult = cloudinaryService.uploadFile(file, "homestay_images");
                String imageUrl = uploadResult.get("secure_url").toString();
                String publicId = uploadResult.get("public_id").toString();

                // Xác định ảnh Thumbnail: Nếu Homestay chưa có thumbnail thì lấy ảnh ĐẦU TIÊN làm thumbnail
                boolean isThumbnail = !hasThumbnail;
                if (isThumbnail) {
                    hasThumbnail = true; // Đánh dấu là đã có để các ảnh sau không bị set trùng
                }

                // Lưu thông tin vào Database
                PropertyImage propertyImage = PropertyImage.builder()
                        .property(property)
                        .imageUrl(imageUrl)
                        .publicId(publicId)
                        .isThumbnail(isThumbnail)
                        .build();

                savedImages.add(propertyImageRepository.save(propertyImage));

            } catch (Exception e) {
                log.error("Lỗi khi upload 1 file ảnh cho Homestay {}: {}", propertyId, e.getMessage());
                // Tùy nghiệp vụ: Bạn có thể throw Exception để rollback, hoặc bỏ qua file lỗi để up tiếp file khác
            }
        }
        log.info("Đã upload thành công {} ảnh cho Homestay ID: {}", savedImages.size(), propertyId);
        return savedImages;
    }
}