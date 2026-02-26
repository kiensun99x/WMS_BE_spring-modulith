package com.rk.WMS.auth;

import com.rk.WMS.auth.dto.request.LoginRequest;
import com.rk.WMS.auth.dto.response.LoginResponse;
import com.rk.WMS.auth.event.UserLoginSuccessEvent;
import com.rk.WMS.auth.mapper.AuthMapper;
import com.rk.WMS.auth.model.User;
import com.rk.WMS.auth.repository.UserRepository;
import com.rk.WMS.auth.service.Impl.AuthServiceImpl;
import com.rk.WMS.common.constants.UserStatus;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.config.JwtTokenConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for AuthServiceImpl
 * Mục đích: Kiểm tra business logic của service đăng nhập
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenConfig jwtTokenConfig;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuthServiceImpl authService;

    @Captor
    private ArgumentCaptor<UserLoginSuccessEvent> eventCaptor;

    private LoginRequest loginRequest;
    private User user;
    private LoginResponse loginResponse;

    /**
     * Setup trước mỗi test case
     * Khởi tạo dữ liệu mẫu
     */
    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest(
                "testuser",
                "password123",
                1L
        );

        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .fullName("Test User")
                .status(UserStatus.ACTIVE)
                .warehouse(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        loginResponse = LoginResponse.builder()
                .userId(1L)
                .username("testuser")
                .fullName("Test User")
                .warehouseId(1L)
                .accessToken("jwt-token")
                .authenticated(true)
                .build();
    }

    /**
     * Test case: Đăng nhập thành công
     * Luồng xử lý:
     * 1. Tìm user theo username -> thành công
     * 2. Check password match -> thành công
     * 3. Check user active -> thành công
     * 4. Check warehouse match -> thành công
     * 5. Generate token -> thành công
     * 6. Map response -> thành công
     * 7. Publish event -> thành công
     */
    @Test
    @DisplayName("Login - Thành công")
    void login_Success() {
        // Given
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);
        when(jwtTokenConfig.generateToken(anyString(), anyLong(), anyLong()))
                .thenReturn("jwt-token");
        when(authMapper.toLoginResponse(any(User.class)))
                .thenReturn(loginResponse);

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("jwt-token", response.getAccessToken());
        assertTrue(response.isAuthenticated());

        // Verify event đã publish với đúng thông tin
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        UserLoginSuccessEvent publishedEvent = eventCaptor.getValue();
        assertEquals(1L, publishedEvent.getUserId());
        assertEquals(1L, publishedEvent.getWarehouseId());
        assertEquals("testuser", publishedEvent.getUsername());
    }

    /**
     * Test case: Đăng nhập thất bại - Không tìm thấy user
     * Expected: Throw AppException với code ACCOUNT_NOT_FOUND
     */
    @Test
    @DisplayName("Login - Tài khoản không tồn tại")
    void login_UserNotFound() {
        // Given
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> authService.login(loginRequest));

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
        verify(userRepository).findByUsername("testuser");
        verifyNoInteractions(passwordEncoder, jwtTokenConfig, eventPublisher);
    }

    /**
     * Test case: Đăng nhập thất bại - Sai mật khẩu
     * Expected: Throw AppException với code INVALID_LOGIN_INFO
     */
    @Test
    @DisplayName("Login - Mật khẩu không đúng")
    void login_InvalidPassword() {
        // Given
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);  // Password không match

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> authService.login(loginRequest));

        assertEquals(ErrorCode.INVALID_LOGIN_INFO, exception.getErrorCode());
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verifyNoInteractions(jwtTokenConfig, eventPublisher);
    }

//    /**
//     * Test case: Đăng nhập thất bại - Tài khoản bị vô hiệu hóa
//     * Expected: Throw AppException với code INVALID_LOGIN_INFO
//     */
//    @Test
//    @DisplayName("Login - Tài khoản bị vô hiệu hóa")
//    void login_UserDisabled() {
//        // Given - User status = INACTIVE
//        user.setStatus(UserStatus.INACTIVE);
//        when(userRepository.findByUsername(anyString()))
//                .thenReturn(Optional.of(user));
//        when(passwordEncoder.matches(anyString(), anyString()))
//                .thenReturn(true);
//
//        // When & Then
//        AppException exception = assertThrows(AppException.class,
//                () -> authService.login(loginRequest));
//
//        assertEquals(ErrorCode.INVALID_LOGIN_INFO, exception.getErrorCode());
//        verifyNoInteractions(jwtTokenConfig, eventPublisher);
//    }

    /**
     * Test case: Đăng nhập thất bại - User không có warehouse
     * Expected: Throw AppException với code INVALID_LOGIN_INFO
     */
    @Test
    @DisplayName("Login - User không có warehouse")
    void login_UserWithoutWarehouse() {
        // Given - User không được gán warehouse
        user.setWarehouse(null);
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> authService.login(loginRequest));

        assertEquals(ErrorCode.INVALID_LOGIN_INFO, exception.getErrorCode());
        verifyNoInteractions(jwtTokenConfig, eventPublisher);
    }

    /**
     * Test case: Đăng nhập thất bại - WarehouseId không khớp
     * Expected: Throw AppException với code INVALID_LOGIN_INFO
     */
    @Test
    @DisplayName("Login - Warehouse không hợp lệ")
    void login_InvalidWarehouseId() {
        // Given
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);

        // Đổi warehouseId trong request không khớp với user
        loginRequest.setWarehouseId(999L);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> authService.login(loginRequest));

        assertEquals(ErrorCode.INVALID_LOGIN_INFO, exception.getErrorCode());
        verifyNoInteractions(jwtTokenConfig, eventPublisher);
    }
}