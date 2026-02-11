package com.rk.WMS.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtTokenConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;


    /**
     * Tạo JWT access token cho user.
     *
     * @param username    username (được set làm subject của token)
     * @param userId      ID user (custom claim)
     * @param warehouseId ID warehouse (custom claim, có thể null)
     * @return JWT token dạng String
     */
    public String generateToken(String username, Long userId, Integer warehouseId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("warehouseId", warehouseId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(SignatureAlgorithm.HS256, getSignKey())
                .compact();
    }

    /**
     * Extract userId từ JWT token.
     *
     * @param token JWT token
     * @return userId lấy từ claim
     */
    public Integer extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Integer.class));
    }

    /**
     * Extract warehouseId từ JWT token.
     *
     * @param token JWT token
     * @return warehouseId lấy từ claim
     */
    public Integer extractWarehouseId(String token) {
        return extractClaim(token, claims -> claims.get("warehouseId", Integer.class));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse JWT token và lấy toàn bộ claims.
     *
     * - Verify chữ ký (signature)
     * - Check expiration
     *
     * @param token JWT token
     * @return Claims đã được verify
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Tạo secret key dùng cho HS256 từ chuỗi secret.
     *
     * @return Key dùng để sign và verify JWT
     */
    private Key getSignKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Validate JWT token.
     *
     * Method này:
     * - Verify signature
     * - Check expiration
     *
     * @param token JWT token
     * @return true nếu token hợp lệ, false nếu token không hợp lệ
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract username (subject) từ JWT token.
     *
     * @param token JWT token
     * @return username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

}
