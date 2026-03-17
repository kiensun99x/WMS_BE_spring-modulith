package com.rk.WMS.auth.service.Impl;

import com.rk.WMS.auth.dto.request.LoginRequest;
import com.rk.WMS.auth.dto.response.LoginResponse;
import com.rk.WMS.auth.event.UserLoginSuccessEvent;
import com.rk.WMS.auth.mapper.AuthMapper;
import com.rk.WMS.auth.model.User;
import com.rk.WMS.auth.repository.UserRepository;
import com.rk.WMS.auth.service.AuthService;
import com.rk.WMS.common.constants.UserStatus;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.config.JwtTokenConfig;
import com.rk.WMS.warehouse.model.Warehouse;
import com.rk.WMS.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j(topic = "AUTH-SERVICE")
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenConfig jwtTokenConfig;
    private final AuthMapper authMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final WarehouseService warehouseService;


    /**
     * Xử lý đăng nhập người dùng vào hệ thống.
     *
     * Luồng xử lý:
     * 1. Nhận thông tin đăng nhập (username, password, warehouseId)
     * 2. Kiểm tra tài khoản tồn tại
     * 3. Xác thực trạng thái tài khoản, mật khẩu và kho làm việc
     * 4. Gen JWT AT token nếu đăng nhập hợp lệ
     * 5. Publish event đăng nhập thành công
     *
     * @param request thông tin đăng nhập từ client
     * @return LoginResponse chứa token và thông tin người dùng
     */
    public LoginResponse login(LoginRequest request) {

        log.info("[LOGIN] Request | username={}, warehouseId={}",
                request.getUsername(), request.getWarehouseId());



        // - Tìm user theo username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        /**
         * Validate thông tin đăng nhập:
         * - Trạng thái tài khoản phải đang active (status == 1)
         * - Mật khẩu nhập vào phải match với mật khẩu đã mã hóa trong DB
         * - User phải thuộc đúng warehouse đang đăng nhập
         */
        if (user.getStatus() != UserStatus.ACTIVE ||
                !passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            throw new AppException(ErrorCode.INVALID_LOGIN_INFO);
        }

        if (user.getWarehouse() == null ||
                !user.getWarehouse().equals(request.getWarehouseId())) {
            throw new AppException(ErrorCode.INVALID_LOGIN_INFO);
        }


        // Gen JWT AT token
        String token = jwtTokenConfig.generateToken(
                user.getUsername(),
                user.getId(),
                request.getWarehouseId()
        );


        //Map thông tin user sang LoginResponse
        LoginResponse response = authMapper.toLoginResponse(user);
        response.setAccessToken(token);
        response.setAuthenticated(true);

        //Map thông tin warehouse
        Warehouse warehouse = warehouseService.getById(request.getWarehouseId());
        response.setWarehouseCode(warehouse.getWarehouseCode());
        response.setWarehouseName(warehouse.getName());


        // Publish event
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