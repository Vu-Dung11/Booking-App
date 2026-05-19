package com.example.bookingapp.service;

import com.example.bookingapp.configuration.utils.SecurityUtils;
import com.example.bookingapp.dto.UserResponse;
import com.example.bookingapp.entity.User;
import com.example.bookingapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public UserResponse getCurrentUserResponse() {
        return UserResponse.fromEntity(securityUtils.getCurrentUser());
    }

    public Page<UserResponse> getAllUsers(User.Role role, Pageable pageable) {
        Page<User> usersPage;
        if (role != null) {
            usersPage = userRepository.findByRole(role, pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }
        return usersPage.map(UserResponse::fromEntity);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.fromEntity(user);
    }
}
