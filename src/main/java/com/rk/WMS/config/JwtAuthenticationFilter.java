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

            String token = getJwtFromRequest(request);

            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

                String username = jwtTokenProvider.extractUsername(token);
                Long userId = jwtTokenProvider.extractUserId(token);
                Long warehouseId = jwtTokenProvider.extractWarehouseId(token);

                CustomUserPrincipal principal =
                        new CustomUserPrincipal(
                                userId,
                                username,
                                warehouseId,
                                null
                        );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                null
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authenticated userId={}, warehouseId={}", userId, warehouseId);
            }

        } catch (Exception ex) {
            log.error("Cannot set authentication", ex);
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
