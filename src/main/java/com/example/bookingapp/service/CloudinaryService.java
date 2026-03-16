package com.example.bookingapp.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Hàm Upload ảnh lên Cloudinary
     * @param file: File ảnh nhận từ người dùng (kiểu MultipartFile)
     * @param folderName: Tên thư mục trên Cloudinary (VD: "avatars", "properties")
     * @return Map chứa "secure_url" (Link ảnh) và "public_id" (Mã ảnh để sau này xóa)
     */
    public Map<String, Object> uploadFile(MultipartFile file, String folderName) throws IOException {
        try {
            // Cấu hình thư mục lưu trữ trên Cloudinary
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", folderName
            );

            // Thực hiện upload và lấy kết quả trả về
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            log.info("Upload ảnh thành công lên folder: {}", folderName);
            return uploadResult;

        } catch (IOException e) {
            log.error("Lỗi khi upload ảnh lên Cloudinary: {}", e.getMessage());
            throw new IOException("Không thể upload file: " + e.getMessage());
        }
    }

    /**
     * Hàm Xóa ảnh trên Cloudinary (Dùng khi người dùng muốn đổi avatar hoặc xóa ảnh Homestay)
     * @param publicId: Mã định danh của ảnh lấy từ Database
     */
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Đã xóa ảnh có public_id: {}", publicId);
        } catch (IOException e) {
            log.error("Lỗi khi xóa ảnh trên Cloudinary: {}", e.getMessage());
        }
    }
}