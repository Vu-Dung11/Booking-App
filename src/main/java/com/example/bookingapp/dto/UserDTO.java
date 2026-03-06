package com.example.bookingapp.dto;



import com.example.bookingapp.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class UserDTO {
    private UUID id;

    private String username;

    private String email;

    private String password;

    private User.Role role;

    private LocalDateTime createdAt;

    private LocalDateTime updateAt;
}
