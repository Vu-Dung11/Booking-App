package com.example.bookingapp.dto.admin;

import com.example.bookingapp.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Update không cho đổi email & password. Để đổi password cần endpoint reset riêng. */
@Getter
@Setter
public class AdminUserUpdateRequest {

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private String phoneNumber;

    @NotNull(message = "Vui lòng chọn role")
    private User.Role role;
}
