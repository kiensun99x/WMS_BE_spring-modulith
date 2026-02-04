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

import java.util.HashMap;
import java.util.Map;

@Slf4j(topic = "AUTH-CONTROLLER")
@RestController
@RequestMapping("/api/v1/auth")
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

//    @GetMapping("/test-token")
//    public ApiResponse<Map<String, Object>> testToken(
//            @RequestAttribute("userId") Integer userId,
//            @RequestAttribute("username") String username,
//            @RequestAttribute("warehouseId") Integer warehouseId) {
//
//        log.info("[TEST-TOKEN] Valid token - userId: {}, username: {}, warehouseId: {}",
//                userId, username, warehouseId);
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("status", "SUCCESS");
//        result.put("message", "Token hợp lệ");
//        result.put("userId", userId);
//        result.put("username", username);
//        result.put("warehouseId", warehouseId);
//        result.put("timestamp", System.currentTimeMillis());
//
//        return ApiResponse.<Map<String, Object>>builder()
//                .code(ErrorCode.SUCCESS.getCode())
//                .message("Token hợp lệ")
//                .result(result)
//                .build();
//    }

}