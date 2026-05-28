package com.example.bookingapp.configuration.enm;


import lombok.Getter;

@Getter
public enum ErrorCode {

    USER_NOT_FOUND(404, "Không tìm thấy người dùng"),
    FILE_EMPTY(511, "Không nhận được file ảnh"),
    ROOM_FULLY_BOOKED(400, "Phòng đã được đặt hết trong khoảng thời gian này"),
    UNAUTHORIZED(401, "Bạn không có quyền"),
    INVALID_INPUT(400, "Dữ liệu đầu vào không hợp lệ"),
    CHECK_OUT_MUST_BE_AFTER_CHECK_IN(506,"ngày nhận phòng phải trước ngày trả phòng"),
    INVALID_PASSWORD_OR_EMAIL(405, "Sai mật khẩu hoặc email"),
    ROOM_IS_NOT_FOUND(507,"Không tìm thấy phòng với id đó"),
    USER_NOT_AUTHENTICATED(405, "User not authenticated"),
    PROPERTY_NOT_FOUND(505, "Không tìm thấy homestay"),
    BOOKING_NOT_FOUND(506, "Không tìm thấy Booking"),
    NOT_YOUR_BOOKING(507, "Bạn không thể đánh giá đơn hàng của người khác"),
    NOT_IN_PENDING_STATUS(510, "Đơn hàng không ở trạng thái chờ thanh toán"),
    BOOKING_IS_NOT_COMPLETED(508, " Bạn chỉ được đánh giá sau khi đã hoàn tất chuyến đi (Check-out)"),
    EXISTED_REVIEW_FOR_BOOKING(509,  "Đơn hàng này đã được đánh giá trước đó"),
    NOT_PROPERTY_OWNER(403, "Bạn không phải chủ sở hữu của homestay này"),
    ONLY_HOST_ALLOWED(403, "Tính năng này chỉ dành cho tài khoản chủ homestay (HOST)"),
    IMAGE_NOT_FOUND(404, "Không tìm thấy ảnh"),
    IMAGE_NOT_BELONG_TO_PROPERTY(400, "Ảnh không thuộc về homestay này"),
    IMAGE_NOT_BELONG_TO_ROOM(400, "Ảnh không thuộc về phòng này"),
    ROOM_NOT_BELONG_TO_PROPERTY(400, "Phòng không thuộc về homestay này"),
    ROOM_HAS_BOOKING(409, "Không thể xoá phòng vì còn đơn đặt phòng. Hãy chờ các đơn hoàn tất hoặc huỷ trước."),
    INVALID_BOOKING_STATUS_FOR_CANCEL(410, "Đơn hàng đã hoàn tất hoặc đã huỷ trước đó, không thể huỷ thêm."),
    INVENTORY_EXCEEDS_CAPACITY(411, "Số phòng trống vượt quá tổng số phòng vật lý hoặc nhỏ hơn số phòng đã được đặt."),
    INVALID_DATE_RANGE(412, "Khoảng ngày không hợp lệ."),
    CHAT_SESSION_NOT_FOUND(512, "Không tìm thấy session chat"),
    GEMINI_API_ERROR(513, "Lỗi gọi Ollama API"),
    CHAT_TOOL_EXECUTION_FAILED(514, "Lỗi thực thi function call"),
    USER_ALREADY_LOCKED(515, "Tài khoản đã bị khoá"),
    USER_ALREADY_ACTIVE(516, "Tài khoản đang hoạt động"),
    CANNOT_LOCK_SELF(517, "Không thể tự khoá tài khoản admin của chính mình"),
    REVIEW_NOT_FOUND(518, "Không tìm thấy review"),
    EMAIL_ALREADY_EXISTS(519, "Email đã được sử dụng"),
    CANNOT_DELETE_SELF(520, "Không thể tự xoá tài khoản của chính mình"),
    USER_HAS_RELATED_DATA(521, "Không thể xoá: tài khoản còn dữ liệu liên quan (homestay/đặt phòng/đánh giá). Hãy khoá tài khoản thay thế.");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


}
