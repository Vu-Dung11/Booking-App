package com.example.bookingapp.dto.admin;

import com.example.bookingapp.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private User.Role role;
    private Boolean isActive;

    public static AdminUserResponse fromEntity(User user) {
        if (user == null) return null;
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .build();
    }
}
