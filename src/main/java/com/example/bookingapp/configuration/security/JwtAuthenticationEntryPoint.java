package com.example.bookingapp.configuration.security;

import com.example.bookingapp.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Tra ve 401 Unauthorized (kem body ApiResponse) khi request chua xac thuc
 * truy cap endpoint can dang nhap — vi du token JWT het han hoac khong hop le.
 * Khong cau hinh cai nay thi Spring Security mac dinh tra 403, khien client
 * (Android/Angular) khong phan biet duoc loi het han token de tu dong dang xuat.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> body = ApiResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                "Phien dang nhap da het han hoac khong hop le");

        objectMapper.writeValue(response.getWriter(), body);
    }
}
