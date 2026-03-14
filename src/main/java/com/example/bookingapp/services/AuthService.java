package com.example.bookingapp.services;

import com.example.bookingapp.dto.AuthResponse;
import com.example.bookingapp.enm.ErrorCode;
import com.example.bookingapp.entity.User;
import com.example.bookingapp.exception.AppException;
import com.example.bookingapp.form.LoginRequest;
import com.example.bookingapp.form.RegisterRequest;
import com.example.bookingapp.repository.UserRepository;
import com.example.bookingapp.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(User.Role.GUEST)
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(token)
//                .email(user.getEmail())
//                .fullName(user.getFullName())
//                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_PASSWORD_OR_EMAIL));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD_OR_EMAIL);
        }

        String token = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(token)
//                .email(user.getEmail())
//                .fullName(user.getFullName())
//                .role(user.getRole().name())
                .build();
    }
}
