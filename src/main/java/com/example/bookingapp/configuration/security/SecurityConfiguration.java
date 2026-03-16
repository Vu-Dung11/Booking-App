package com.example.bookingapp.configuration.security;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Cho phép dùng @PreAuthorize("hasRole('ADMIN')") trên Controller
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;

    // Khởi tạo Bean mã hóa mật khẩu (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cấu hình chuỗi bộ lọc bảo mật
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF vì chúng ta dùng JWT (stateless)
                .cors(AbstractHttpConfigurer::disable) // Tạm tắt CORS (Sau này có Frontend thì mở lại)
                .authorizeHttpRequests(auth -> auth
                        // Public các API liên quan đến xác thực và tìm kiếm chung
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // .requestMatchers(HttpMethod.GET, "/api/v1/properties/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/v1/media/**").permitAll()
                        // Các API khác yêu cầu phải đăng nhập
                        .requestMatchers("/api/v1/payments/**").permitAll()
                        .anyRequest().authenticated()
                )
                // Không lưu trữ session (Session Creation Policy = STATELESS)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Chèn bộ lọc JWT của chúng ta vào trước bộ lọc mặc định của Spring Security
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}