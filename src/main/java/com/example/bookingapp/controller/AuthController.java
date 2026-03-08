package com.example.bookingapp.controller;


import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.dto.AuthResponse;
import com.example.bookingapp.form.LoginRequest;
import com.example.bookingapp.form.RegisterRequest;
import com.example.bookingapp.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse data = authService.register(registerRequest);
        return ApiResponse.success(data);
    }
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse data = authService.login(loginRequest);
        return ApiResponse.success(data);
    }


}
