package com.example.bookingapp.service;

import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.configuration.utils.SecurityUtils;
import com.example.bookingapp.entity.Room;
import com.example.bookingapp.entity.RoomImage;
import com.example.bookingapp.entity.User;
import com.example.bookingapp.repository.RoomImageRepository;
import com.example.bookingapp.repository.RoomRepository;
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
public class RoomImageService {

    private final RoomImageRepository roomImageRepository;
    private final RoomRepository roomRepository;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    /**
     * Verify room thuộc property và thuộc host hiện tại.
     */
    private Room requireOwnedRoom(Long propertyId, Long roomId) {
        User host = securityUtils.getCurrentUser();
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_IS_NOT_FOUND));
        if (!room.getProperty().getId().equals(propertyId)) {
            throw new AppException(ErrorCode.ROOM_NOT_BELONG_TO_PROPERTY);
        }
        if (!room.getProperty().getHost().getId().equals(host.getId())) {
            throw new AppException(ErrorCode.NOT_PROPERTY_OWNER);
        }
        return room;
    }

    @Transactional
    public List<RoomImage> uploadImagesForRoom(Long propertyId, Long roomId, List<MultipartFile> files) {
        Room room = requireOwnedRoom(propertyId, roomId);

        boolean hasThumbnail = roomImageRepository.existsByRoomIdAndIsThumbnailTrue(roomId);
        List<RoomImage> savedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            try {
                Map<String, Object> uploadResult = cloudinaryService.uploadFile(file, "room_images");
                String imageUrl = uploadResult.get("secure_url").toString();
                String publicId = uploadResult.get("public_id").toString();

                boolean isThumbnail = !hasThumbnail;
                if (isThumbnail) hasThumbnail = true;

                RoomImage roomImage = RoomImage.builder()
                        .room(room)
                        .imageUrl(imageUrl)
                        .publicId(publicId)
                        .isThumbnail(isThumbnail)
                        .build();
                savedImages.add(roomImageRepository.save(roomImage));
            } catch (Exception e) {
                log.error("Lỗi khi upload ảnh cho Phòng {}: {}", roomId, e.getMessage());
            }
        }
        log.info("Đã upload thành công {} ảnh cho Phòng ID: {}", savedImages.size(), roomId);
        return savedImages;
    }

    /** Backwards-compatible wrapper cho RoomImageController cũ (không có propertyId trên path). */
    @Transactional
    public List<RoomImage> uploadImagesForRoom(Long roomId, List<MultipartFile> files) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_IS_NOT_FOUND));
        return uploadImagesForRoom(room.getProperty().getId(), roomId, files);
    }

    @Transactional(readOnly = true)
    public List<RoomImage> listImagesForRoom(Long propertyId, Long roomId) {
        requireOwnedRoom(propertyId, roomId);
        return roomImageRepository.findByRoomId(roomId);
    }

    /** Trả về danh sách URL ảnh cho 1 room — thumbnail trước, các ảnh khác sau. */
    @Transactional(readOnly = true)
    public List<String> getImageUrlsForRoom(Long roomId) {
        List<RoomImage> all = roomImageRepository.findByRoomId(roomId);
        all.sort((a, b) -> Boolean.compare(
                !Boolean.TRUE.equals(b.getIsThumbnail()),
                !Boolean.TRUE.equals(a.getIsThumbnail())));
        return all.stream().map(RoomImage::getImageUrl).toList();
    }

    @Transactional
    public void deleteImage(Long propertyId, Long roomId, Long imageId) {
        requireOwnedRoom(propertyId, roomId);
        RoomImage image = roomImageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        if (!image.getRoom().getId().equals(roomId)) {
            throw new AppException(ErrorCode.IMAGE_NOT_BELONG_TO_ROOM);
        }

        boolean wasThumbnail = Boolean.TRUE.equals(image.getIsThumbnail());
        cloudinaryService.deleteFile(image.getPublicId());
        roomImageRepository.delete(image);

        if (wasThumbnail) {
            List<RoomImage> remaining = roomImageRepository.findByRoomId(roomId);
            if (!remaining.isEmpty()) {
                RoomImage next = remaining.get(0);
                next.setIsThumbnail(true);
                roomImageRepository.save(next);
            }
        }
    }

    @Transactional
    public RoomImage setThumbnail(Long propertyId, Long roomId, Long imageId) {
        requireOwnedRoom(propertyId, roomId);
        RoomImage target = roomImageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        if (!target.getRoom().getId().equals(roomId)) {
            throw new AppException(ErrorCode.IMAGE_NOT_BELONG_TO_ROOM);
        }

        List<RoomImage> all = roomImageRepository.findByRoomId(roomId);
        for (RoomImage img : all) {
            if (img.getId().equals(imageId)) {
                img.setIsThumbnail(true);
            } else if (Boolean.TRUE.equals(img.getIsThumbnail())) {
                img.setIsThumbnail(false);
            }
        }
        roomImageRepository.saveAll(all);
        return roomImageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
    }
}
