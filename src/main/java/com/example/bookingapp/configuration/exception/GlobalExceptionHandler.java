package com.example.bookingapp.configuration.exception;


import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.configuration.enm.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bắt các lỗi nghiệp vụ do mình chủ động ném ra (VD: Hết phòng)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse<Void> response = ApiResponse.error(errorCode.getCode(), errorCode.getMessage());
        // Trả về HTTP Status 400 (Bad Request) nhưng body vẫn là format ApiResponse của mình
        return ResponseEntity.badRequest().body(response);
    }

    // 2. Bắt lỗi Validation (Khi dùng @Valid cho các DTO đầu vào bị sai)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        // Lấy câu thông báo lỗi đầu tiên từ DTO (VD: "Email không được để trống")
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ApiResponse<Void> response = ApiResponse.error(400, errorMessage);
        return ResponseEntity.badRequest().body(response);
    }

    // 3. Bắt tất cả các lỗi hệ thống không lường trước được (VD: NullPointerException, Mất kết nối DB)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnwantedException(Exception e) {
        ApiResponse<Void> response = ApiResponse.error(500, "Lỗi hệ thống không xác định: " + e.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }
}
