package com.example.bookingapp.enm;


import lombok.Getter;

@Getter
public enum ErrorCode {

    USER_NOT_FOUND(404, "Không tìm thấy người dùng"),
    ROOM_FULLY_BOOKED(400, "Phòng đã được đặt hết trong khoảng thời gian này"),
    UNAUTHORIZED(401, "Bạn không có quyền"),
    INVALID_INPUT(400, "Dữ liệu đầu vào không hợp lệ"),
    INVALID_PASSWORD_OR_EMAIL(405, "Sai mật khẩu hoặc email"),
    USER_NOT_AUTHENTICATED(405, "User not authenticated"),
    PROPERTY_NOT_FOUND(505, "Không tìm thấy homestay");
    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


}
