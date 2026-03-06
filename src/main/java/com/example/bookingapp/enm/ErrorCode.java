package com.example.bookingapp.enm;


import lombok.Getter;

@Getter
public enum ErrorCode {

    USER_NOT_FOUND(404, "Không tìm thấy người dùng"),
    ROOM_FULLY_BOOKED(400, "Phòng đã được đặt hết trong khoảng thời gian này"),
    UNAUTHORIZED(401, "Bạn chưa đăng nhập hoặc không có quyền"),
    INVALID_INPUT(400, "Dữ liệu đầu vào không hợp lệ");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


}
