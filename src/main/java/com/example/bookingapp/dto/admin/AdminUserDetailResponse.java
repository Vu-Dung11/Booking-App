package com.example.bookingapp.dto.admin;

import com.example.bookingapp.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserDetailResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private User.Role role;
    private Boolean isActive;

    // Stats theo role: HOST -> property count; GUEST -> booking + review count
    private Long propertyCount;
    private Long bookingCount;
    private Long reviewCount;
}
