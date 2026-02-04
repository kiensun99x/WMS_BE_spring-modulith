package com.rk.WMS.auth.service;

import com.rk.WMS.auth.dto.request.LoginRequest;
import com.rk.WMS.auth.dto.response.LoginResponse;
import com.rk.WMS.auth.event.UserLoginSuccessEvent;
import com.rk.WMS.auth.mapper.AuthMapper;
import com.rk.WMS.auth.model.User;
import com.rk.WMS.auth.repository.UserRepository;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.config.JwtTokenConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j(topic = "AUTH-SERVICE")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenConfig jwtTokenConfig;
    private final AuthMapper authMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LoginResponse login(LoginRequest request) {

        log.info("[LOGIN] Request | username={}, warehouseId={}",
                request.getUsername(), request.getWarehouseId());

        User user = userRepository.findByUsernameWithWarehouse(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("[LOGIN][FAILED] Invalid login info | username={}",
                            request.getUsername());
                    return new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
                });

        if (user.getStatus() != 1 ||
                !passwordEncoder.matches(request.getPassword(), user.getPassword()) ||
                user.getWarehouse() == null ||
                !user.getWarehouse().getWarehouseId().equals(request.getWarehouseId()) ||
                user.getWarehouse().getStatus() != 1) {

            log.warn("[LOGIN][FAILED] Invalid login info | userId={}, warehouseId={}",
                    user.getId(), request.getWarehouseId());

            throw new AppException(ErrorCode.INVALID_LOGIN_INFO);
        }

        String token = jwtTokenConfig.generateToken(
                user.getUsername(),
                user.getId(),
                request.getWarehouseId()
        );

        LoginResponse response = authMapper.toLoginResponse(user);
        response.setAccessToken(token);
        response.setAuthenticated(true);

        /// Public login event
        eventPublisher.publishEvent(
                new UserLoginSuccessEvent(
                        this,
                        user.getId(),
                        request.getWarehouseId(),
                        user.getUsername()
                )
        );

        log.info("[LOGIN][SUCCESS] Login success | userId={}, warehouseId={}",
                user.getId(), request.getWarehouseId());

        return response;
    }
}