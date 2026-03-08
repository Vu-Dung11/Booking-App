package com.example.bookingapp.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

public class AuthResponse {
    private String token;
}
