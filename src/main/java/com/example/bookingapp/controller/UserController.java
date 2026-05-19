package com.example.bookingapp.controller;

import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.dto.UserResponse;
import com.example.bookingapp.entity.User;
import com.example.bookingapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ApiResponse<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) User.Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.getAllUsers(role, pageable);
        return ApiResponse.success(users);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.success(userService.getCurrentUserResponse());
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ApiResponse.success(user);
    }
}
