package com.example.bookingapp.controller;

import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.dto.admin.AdminUserCreateRequest;
import com.example.bookingapp.dto.admin.AdminUserDetailResponse;
import com.example.bookingapp.dto.admin.AdminUserResponse;
import com.example.bookingapp.dto.admin.AdminUserStatsResponse;
import com.example.bookingapp.dto.admin.AdminUserUpdateRequest;
import com.example.bookingapp.entity.User;
import com.example.bookingapp.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<Page<AdminUserResponse>> list(
            @RequestParam(required = false) User.Role role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(adminUserService.search(role, isActive, keyword, pageable));
    }

    @GetMapping("/stats")
    public ApiResponse<AdminUserStatsResponse> stats() {
        return ApiResponse.success(adminUserService.stats());
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminUserDetailResponse> getDetail(@PathVariable Long id) {
        return ApiResponse.success(adminUserService.getDetail(id));
    }

    @PatchMapping("/{id}/lock")
    public ApiResponse<AdminUserResponse> lock(@PathVariable Long id) {
        return ApiResponse.success(adminUserService.lock(id));
    }

    @PatchMapping("/{id}/unlock")
    public ApiResponse<AdminUserResponse> unlock(@PathVariable Long id) {
        return ApiResponse.success(adminUserService.unlock(id));
    }

    @PostMapping
    public ApiResponse<AdminUserResponse> create(@Valid @RequestBody AdminUserCreateRequest request) {
        return ApiResponse.success(adminUserService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminUserResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody AdminUserUpdateRequest request) {
        return ApiResponse.success(adminUserService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        adminUserService.delete(id);
        return ApiResponse.success("Đã xoá tài khoản");
    }
}
