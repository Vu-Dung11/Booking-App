package com.example.bookingapp.security;



import com.example.bookingapp.Utils.JwtUtil;
import com.example.bookingapp.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Kiểm tra header Authorization có chứa Bearer Token không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Không có thì đi tiếp (Sẽ bị block ở SecurityConfig nếu API yêu cầu quyền)
            return;
        }

        // 2. Lấy chuỗi JWT (bỏ chữ "Bearer " ở đầu)
        jwt = authHeader.substring(7);
        userEmail = jwtUtil.extractEmail(jwt); // Giải mã lấy email

        // 3. Nếu có email và user chưa được xác thực trong ngữ cảnh hiện tại
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Tìm user trong DB để đảm bảo user này vẫn còn tồn tại
            userRepository.findByEmail(userEmail).ifPresent(user -> {

                // Kiểm tra token có hợp lệ không
                if (jwtUtil.isTokenValid(jwt, user.getEmail())) {

                    // Tạo đối tượng Authentication và lưu vào SecurityContext
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user, // Principal
                            null, // Credentials (không cần pass)
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())) // Quyền hạn
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Cấp quyền thành công cho request này
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            });
        }

        // Chuyển request đi tiếp tới Controller
        filterChain.doFilter(request, response);
    }
}