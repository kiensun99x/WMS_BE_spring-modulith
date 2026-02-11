package com.rk.WMS.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenConfig jwtTokenProvider;

    /**
     * Filter xử lý JWT authentication.
     *
     * Luồng xử lý:
     *   <li>Lấy JWT từ header Authorization</li>
     *   <li>Validate token</li>
     *   <li>Extract thông tin user từ token</li>
     *   <li>Set Authentication vào SecurityContext</li>
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Lấy JWT token từ HTTP request (Authorization header)
            String token = getJwtFromRequest(request);

            // Kiểm tra token tồn tại và hợp lệ
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.extractUsername(token);
                Integer userId = jwtTokenProvider.extractUserId(token);
                Integer warehouseId = jwtTokenProvider.extractWarehouseId(token);

                // Tạo authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                null
                        );

                // Gắn thêm thông tin request
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Set Authentication vào SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);


                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                request.setAttribute("warehouseId", warehouseId);

                log.debug("Authenticated user: {}, userId: {}, warehouseId: {}",
                        username, userId, warehouseId);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Trích xuất JWT token từ header Authorization.
     *
     * @param request HTTP request
     * @return JWT token hoặc null nếu không tồn tại
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Chỉ định các endpoint KHÔNG cần filter JWT.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/login") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs");
    }
}
