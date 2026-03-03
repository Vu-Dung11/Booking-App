-- 1. Tối ưu hóa luồng tìm kiếm Homestay (Search API)
-- Khách hàng thường xuyên gõ tên thành phố vào thanh tìm kiếm
CREATE INDEX idx_properties_city ON properties(city);

-- Index kết hợp: Thường khi tìm kiếm, hệ thống chỉ lấy các property đang active
CREATE INDEX idx_properties_city_active ON properties(city, is_active);


-- 2. Tối ưu hóa luồng hiển thị phòng
-- Khi khách click vào một Homestay, thường sẽ query các phòng theo giá để sắp xếp (Sort by Price)
CREATE INDEX idx_rooms_base_price ON rooms(base_price);


-- 3. Tối ưu hóa luồng quản lý Đặt phòng (Booking API)
-- Lấy lịch sử đặt phòng của một khách hàng cụ thể theo trạng thái (VD: Tab "Đang xử lý", "Đã hoàn thành")
CREATE INDEX idx_bookings_guest_status ON bookings(guest_id, status);

-- Truy vấn các booking trong một khoảng thời gian (rất cần cho Admin dashboard hoặc Host quản lý lịch)
CREATE INDEX idx_bookings_dates ON bookings(check_in_date, check_out_date);


-- 4. Tối ưu hóa luồng Thanh toán (Payment Webhook)
-- Khi VNPAY hoặc MOMO gọi API callback về hệ thống của bạn, họ sẽ truyền lên transaction_id.
-- Bạn phải dùng mã này để query ra bản ghi payment và update trạng thái. Index này cực kỳ quan trọng để tránh timeout khi IPN call.
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);