package com.example.bookingapp.service;

import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.configuration.utils.SecurityUtils;
import com.example.bookingapp.dto.admin.AdminUserCreateRequest;
import com.example.bookingapp.dto.admin.AdminUserDetailResponse;
import com.example.bookingapp.dto.admin.AdminUserResponse;
import com.example.bookingapp.dto.admin.AdminUserStatsResponse;
import com.example.bookingapp.dto.admin.AdminUserUpdateRequest;
import com.example.bookingapp.entity.User;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.PropertyRepository;
import com.example.bookingapp.repository.ReviewRepository;
import com.example.bookingapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    public Page<AdminUserResponse> search(User.Role role, Boolean isActive, String keyword, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return userRepository.searchForAdmin(role, isActive, kw, pageable)
                .map(AdminUserResponse::fromEntity);
    }

    public AdminUserDetailResponse getDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Long propertyCount = null;
        Long bookingCount = null;
        Long reviewCount = null;

        if (user.getRole() == User.Role.HOST) {
            propertyCount = propertyRepository.countByHostId(user.getId());
        } else if (user.getRole() == User.Role.GUEST) {
            bookingCount = bookingRepository.countByGuest_Id(user.getId());
            reviewCount = reviewRepository.countByBooking_Guest_Id(user.getId());
        }

        return AdminUserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .propertyCount(propertyCount)
                .bookingCount(bookingCount)
                .reviewCount(reviewCount)
                .build();
    }

    @Transactional
    public AdminUserResponse lock(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getId().equals(user.getId())) {
            throw new AppException(ErrorCode.CANNOT_LOCK_SELF);
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new AppException(ErrorCode.USER_ALREADY_LOCKED);
        }

        user.setIsActive(false);
        userRepository.save(user);
        return AdminUserResponse.fromEntity(user);
    }

    @Transactional
    public AdminUserResponse unlock(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new AppException(ErrorCode.USER_ALREADY_ACTIVE);
        }

        user.setIsActive(true);
        userRepository.save(user);
        return AdminUserResponse.fromEntity(user);
    }

    @Transactional
    public AdminUserResponse create(AdminUserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .isActive(true)
                .build();
        return AdminUserResponse.fromEntity(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse update(Long id, AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Không cho admin tự hạ role / khoá quyền của chính mình → tránh tự lock out.
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getId().equals(user.getId()) && request.getRole() != User.Role.ADMIN) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        user.setFullName(request.getFullName().trim());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());
        return AdminUserResponse.fromEntity(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getId().equals(user.getId())) {
            throw new AppException(ErrorCode.CANNOT_DELETE_SELF);
        }

        // Block hard-delete nếu còn dữ liệu liên quan để tránh FK constraint violation.
        // Admin nên dùng lock thay vì delete trong trường hợp này.
        if (user.getRole() == User.Role.HOST
                && propertyRepository.countByHostId(user.getId()) > 0) {
            throw new AppException(ErrorCode.USER_HAS_RELATED_DATA);
        }
        if (user.getRole() == User.Role.GUEST
                && bookingRepository.countByGuest_Id(user.getId()) > 0) {
            throw new AppException(ErrorCode.USER_HAS_RELATED_DATA);
        }

        userRepository.delete(user);
    }

    public AdminUserStatsResponse stats() {
        long totalAdmins = userRepository.countByRole(User.Role.ADMIN);
        long totalHosts = userRepository.countByRole(User.Role.HOST);
        long totalGuests = userRepository.countByRole(User.Role.GUEST);
        long locked = userRepository.countByIsActive(false);
        long active = userRepository.countByIsActive(true);
        return AdminUserStatsResponse.builder()
                .totalUsers(totalAdmins + totalHosts + totalGuests)
                .totalAdmins(totalAdmins)
                .totalHosts(totalHosts)
                .totalGuests(totalGuests)
                .lockedUsers(locked)
                .activeUsers(active)
                .build();
    }
}
