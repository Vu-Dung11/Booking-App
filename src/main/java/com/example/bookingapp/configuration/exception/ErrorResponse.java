package com.example.bookingapp.configuration.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
   
    private String timestamp;
    private String message;
    private Map<String,String> detail;

    public ErrorResponse(String message, Map<String, String> detail) {
        this.timestamp = LocalDateTime.now().toString();
        this.message = message;
        this.detail = detail;
    }
    /*
     * 
     *  timestamp": "12:00:00"
     *  "messenge": "Dữ liệu không hợp lệ"
     *  "detail": {
     *     "key1": "value1"
     *  }
     * 
     * 
     */
}
