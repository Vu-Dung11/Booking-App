


-- 1. Bảng users
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       phone_number VARCHAR(20),
                       role ENUM('GUEST', 'HOST', 'ADMIN') NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Bảng properties
CREATE TABLE properties (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            host_id BIGINT NOT NULL,
                            name VARCHAR(255) NOT NULL,
                            description TEXT,
                            address VARCHAR(255) NOT NULL,
                            city VARCHAR(100) NOT NULL,
                            country VARCHAR(100) NOT NULL,
                            latitude DECIMAL(9, 6),
                            longitude DECIMAL(9, 6),
                            check_in_time TIME,
                            check_out_time TIME,
                            is_active BOOLEAN DEFAULT TRUE,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (host_id) REFERENCES users(id)
);

-- 3. Bảng rooms
CREATE TABLE rooms (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       property_id BIGINT NOT NULL,
                       room_type VARCHAR(50) NOT NULL,
                       capacity INT NOT NULL,
                       base_price DECIMAL(12, 2) NOT NULL,
                       quantity INT NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (property_id) REFERENCES properties(id)
);

-- 4. Bảng room_inventory (Quản lý phòng trống theo ngày)
CREATE TABLE room_inventory (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                room_id BIGINT NOT NULL,
                                inventory_date DATE NOT NULL,
                                available_count INT NOT NULL,
                                UNIQUE KEY unique_room_date (room_id, inventory_date),
                                CONSTRAINT chk_available_count CHECK (available_count >= 0),
                                FOREIGN KEY (room_id) REFERENCES rooms(id)
);
CREATE INDEX idx_inventory_date ON room_inventory(inventory_date);

-- 5. Bảng bookings
CREATE TABLE bookings (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          guest_id BIGINT NOT NULL,
                          room_id BIGINT NOT NULL,
                          check_in_date DATE NOT NULL,
                          check_out_date DATE NOT NULL,
                          total_price DECIMAL(12, 2) NOT NULL,
                          status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (guest_id) REFERENCES users(id),
                          FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- 6. Bảng payments
CREATE TABLE payments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          booking_id BIGINT NOT NULL,
                          amount DECIMAL(12, 2) NOT NULL,
                          payment_method VARCHAR(50),
                          status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') NOT NULL,
                          transaction_id VARCHAR(100),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- 7. Bảng reviews
CREATE TABLE reviews (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         booking_id BIGINT NOT NULL UNIQUE,
                         property_id BIGINT NOT NULL,
                         rating INT NOT NULL,
                         comment TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT chk_rating CHECK (rating >= 1 AND rating <= 5),
                         FOREIGN KEY (booking_id) REFERENCES bookings(id),
                         FOREIGN KEY (property_id) REFERENCES properties(id)
);