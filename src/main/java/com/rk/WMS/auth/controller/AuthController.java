package com.rk.WMS.auth.controller;

import com.rk.WMS.auth.dto.request.LoginRequest;
import com.rk.WMS.auth.dto.response.LoginResponse;
import com.rk.WMS.auth.service.AuthService;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j(topic = "AUTH-CONTROLLER")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("[LOGIN][API][REQUEST] username={}, warehouseId={}",
                request.getUsername(), request.getWarehouseId());

        LoginResponse response = authService.login(request);

        return ApiResponse.<LoginResponse>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message("Đăng nhập thành công")
                .result(response)
                .build();

    }

}