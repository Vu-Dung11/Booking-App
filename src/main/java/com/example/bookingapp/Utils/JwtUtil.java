package com.example.bookingapp.Utils;



import com.example.bookingapp.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Chuỗi bí mật dùng để ký token (Phải dài ít nhất 256 bits / 32 ký tự)
    // Thực tế nên cấu hình chuỗi này trong application.properties: jwt.secret=...
    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    // Thời gian sống của token (VD: 24 giờ = 1000 * 60 * 60 * 24)
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    // 1. Hàm tạo Token
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name()); // Lưu thêm quyền vào token để phân quyền

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail()) // Dùng email làm định danh chính
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. Hàm lấy Email từ Token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 3. Hàm kiểm tra Token còn hợp lệ không
    public boolean isTokenValid(String token, String userEmail) {
        final String email = extractEmail(token);
        return (email.equals(userEmail)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}