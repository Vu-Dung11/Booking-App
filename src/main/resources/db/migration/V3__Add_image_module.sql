
-- Thêm 2 cột vào bảng users để lưu Avatar
ALTER TABLE users
    ADD COLUMN avatar_url VARCHAR(500),
ADD COLUMN avatar_public_id VARCHAR(100);
CREATE TABLE property_images (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 property_id BIGINT NOT NULL,
                                 image_url VARCHAR(500) NOT NULL,
                                 public_id VARCHAR(100) NOT NULL, -- Rất quan trọng để xóa ảnh trên Cloudinary
                                 is_thumbnail BOOLEAN DEFAULT FALSE, -- Nếu là TRUE thì đây là ảnh đại diện của Homestay
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE
);
CREATE TABLE room_images (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             room_id BIGINT NOT NULL,
                             image_url VARCHAR(500) NOT NULL,
                             public_id VARCHAR(100) NOT NULL,
                             is_thumbnail BOOLEAN DEFAULT FALSE,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);