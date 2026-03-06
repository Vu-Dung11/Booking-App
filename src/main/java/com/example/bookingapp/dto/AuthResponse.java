package com.example.bookingapp.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

public class AuthResponse {
    private String token; // Chuỗi JWT để client lưu lại
    private String email;
    private String fullName;
    private String role;
}
