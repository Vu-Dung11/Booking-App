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

    /** Tìm property và verify ownership (host hiện tại). */
    private Property requireOwnedProperty(Long propertyId) {
        User host = securityUtils.getCurrentUser();
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.PROPERTY_NOT_FOUND));
        if (!property.getHost().getId().equals(host.getId())) {
            throw new AppException(ErrorCode.NOT_PROPERTY_OWNER);
        }
        return property;
    }

    @Transactional
    public List<PropertyImage> uploadImagesForProperty(Long propertyId, List<MultipartFile> files) {
        Property property = requireOwnedProperty(propertyId);

        boolean hasThumbnail = propertyImageRepository.existsByPropertyIdAndIsThumbnailTrue(propertyId);

        List<PropertyImage> savedImages = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            try {
                Map<String, Object> uploadResult = cloudinaryService.uploadFile(file, "homestay_images");
                String imageUrl = uploadResult.get("secure_url").toString();
                String publicId = uploadResult.get("public_id").toString();

                boolean isThumbnail = !hasThumbnail;
                if (isThumbnail) hasThumbnail = true;

                PropertyImage img = PropertyImage.builder()
                        .property(property)
                        .imageUrl(imageUrl)
                        .publicId(publicId)
                        .isThumbnail(isThumbnail)
                        .build();
                savedImages.add(propertyImageRepository.save(img));
            } catch (Exception e) {
                log.error("Lỗi khi upload 1 file ảnh cho Homestay {}: {}", propertyId, e.getMessage());
            }
        }
        log.info("Đã upload thành công {} ảnh cho Homestay ID: {}", savedImages.size(), propertyId);
        return savedImages;
    }

    @Transactional(readOnly = true)
    public List<PropertyImage> listImagesForProperty(Long propertyId) {
        requireOwnedProperty(propertyId);
        return propertyImageRepository.findByPropertyId(propertyId);
    }

    @Transactional
    public void deleteImage(Long propertyId, Long imageId) {
        requireOwnedProperty(propertyId);
        PropertyImage image = propertyImageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        if (!image.getProperty().getId().equals(propertyId)) {
            throw new AppException(ErrorCode.IMAGE_NOT_BELONG_TO_PROPERTY);
        }

        boolean wasThumbnail = Boolean.TRUE.equals(image.getIsThumbnail());

        // Xoá Cloudinary trước, rồi xoá DB. Nếu Cloudinary lỗi vẫn xoá DB để
        // không kẹt orphan record (deleteFile chỉ log lỗi, không throw).
        cloudinaryService.deleteFile(image.getPublicId());
        propertyImageRepository.delete(image);

        // Nếu ảnh bị xoá là thumbnail → set ảnh đầu tiên còn lại làm thumbnail mới
        if (wasThumbnail) {
            List<PropertyImage> remaining = propertyImageRepository.findByPropertyId(propertyId);
            if (!remaining.isEmpty()) {
                PropertyImage next = remaining.get(0);
                next.setIsThumbnail(true);
                propertyImageRepository.save(next);
            }
        }
    }

    @Transactional
    public PropertyImage setThumbnail(Long propertyId, Long imageId) {
        requireOwnedProperty(propertyId);
        PropertyImage target = propertyImageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        if (!target.getProperty().getId().equals(propertyId)) {
            throw new AppException(ErrorCode.IMAGE_NOT_BELONG_TO_PROPERTY);
        }

        // Atomic: bỏ thumbnail flag của tất cả ảnh khác trong property, set ảnh này = true
        List<PropertyImage> all = propertyImageRepository.findByPropertyId(propertyId);
        for (PropertyImage img : all) {
            if (img.getId().equals(imageId)) {
                img.setIsThumbnail(true);
            } else if (Boolean.TRUE.equals(img.getIsThumbnail())) {
                img.setIsThumbnail(false);
            }
        }
        propertyImageRepository.saveAll(all);
        return propertyImageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
    }
}
