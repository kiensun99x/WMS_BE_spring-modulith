package com.rk.WMS.auth;

//
//import com.rk.WMS.auth.dto.request.LoginRequest;
//import com.rk.WMS.auth.dto.response.LoginResponse;
//import com.rk.WMS.auth.event.UserLoginSuccessEvent;
//import com.rk.WMS.auth.mapper.AuthMapper;
//import com.rk.WMS.auth.model.User;
//import com.rk.WMS.auth.repository.UserRepository;
//import com.rk.WMS.auth.service.AuthService;
//import com.rk.WMS.common.exception.AppException;
//import com.rk.WMS.common.exception.ErrorCode;
//import com.rk.WMS.config.JwtTokenConfig;
//import com.rk.WMS.warehouse.model.Warehouse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class AuthServiceTest {
//
//    @InjectMocks
//    private AuthService authService;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private JwtTokenConfig jwtTokenConfig;
//
//    @Mock
//    private AuthMapper authMapper;
//
//    @Mock
//    private ApplicationEventPublisher eventPublisher;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    // ====== Helper ======
//    private User mockValidUser() {
//        Warehouse warehouse = Warehouse.builder()
//                .warehouseId(1)
//                .status(1)
//                .build();
//
//        return User.builder()
//                .id(10)
//                .username("admin")
//                .password("encoded-pass")
//                .status((byte) 1)
//                .warehouse(warehouse)
//                .build();
//    }
//
//    // ==============================
//    // ✅ CASE 1: Login thành công
//    // ==============================
//    @Test
//    void login_success() {
//        LoginRequest request = new LoginRequest("admin", "123456", 1);
//        User user = mockValidUser();
//
//        when(userRepository.findByUsernameWithWarehouse("admin"))
//                .thenReturn(Optional.of(user));
//
//        when(passwordEncoder.matches("123456", "encoded-pass"))
//                .thenReturn(true);
//
//        when(jwtTokenConfig.generateToken("admin", 10, 1))
//                .thenReturn("fake-jwt");
//
//        when(authMapper.toLoginResponse(user))
//                .thenReturn(LoginResponse.builder()
//                        .userId(10)
//                        .username("admin")
//                        .warehouseId(1)
//                        .build());
//
//        LoginResponse response = authService.login(request);
//
//        assertNotNull(response);
//        assertTrue(response.isAuthenticated());
//        assertEquals("fake-jwt", response.getAccessToken());
//
//        verify(eventPublisher, times(1))
//                .publishEvent(any(UserLoginSuccessEvent.class));
//    }
//
//    // ==============================
//    // ❌ CASE 2: Username không tồn tại
//    // ==============================
//    @Test
//    void login_userNotFound() {
//        LoginRequest request = new LoginRequest("unknown", "123", 1);
//
//        when(userRepository.findByUsernameWithWarehouse("unknown"))
//                .thenReturn(Optional.empty());
//
//        AppException ex = assertThrows(AppException.class,
//                () -> authService.login(request));
//
//        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, ex.getErrorCode());
//    }
//
//    // ==============================
//    // ❌ CASE 3: Sai mật khẩu
//    // ==============================
//    @Test
//    void login_wrongPassword() {
//        LoginRequest request = new LoginRequest("admin", "wrong", 1);
//        User user = mockValidUser();
//
//        when(userRepository.findByUsernameWithWarehouse("admin"))
//                .thenReturn(Optional.of(user));
//
//        when(passwordEncoder.matches("wrong", "encoded-pass"))
//                .thenReturn(false);
//
//        AppException ex = assertThrows(AppException.class,
//                () -> authService.login(request));
//
//        assertEquals(ErrorCode.INVALID_LOGIN_INFO, ex.getErrorCode());
//    }
//
//    // ==============================
//    // ❌ CASE 4: Sai warehouse
//    // ==============================
//    @Test
//    void login_wrongWarehouse() {
//        LoginRequest request = new LoginRequest("admin", "123456", 99);
//        User user = mockValidUser();
//
//        when(userRepository.findByUsernameWithWarehouse("admin"))
//                .thenReturn(Optional.of(user));
//
//        when(passwordEncoder.matches("123456", "encoded-pass"))
//                .thenReturn(true);
//
//        AppException ex = assertThrows(AppException.class,
//                () -> authService.login(request));
//
//        assertEquals(ErrorCode.INVALID_LOGIN_INFO, ex.getErrorCode());
//    }
//}


import com.rk.WMS.auth.dto.request.LoginRequest;
import com.rk.WMS.auth.dto.response.LoginResponse;
import com.rk.WMS.auth.event.UserLoginSuccessEvent;
import com.rk.WMS.auth.mapper.AuthMapper;
import com.rk.WMS.auth.model.User;
import com.rk.WMS.auth.repository.UserRepository;
import com.rk.WMS.auth.service.AuthService;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.config.JwtTokenConfig;
import com.rk.WMS.warehouse.model.Warehouse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
    private AuthService authService;

    @Captor
    private ArgumentCaptor<UserLoginSuccessEvent> eventCaptor;

    private LoginRequest loginRequest;
    private User user;
    private Warehouse warehouse;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest(
                "testuser",
                "password123",
                1
        );

        warehouse = Warehouse.builder()
                .warehouseId(1)
                .warehouseCode("WH-001")
                .name("Main Warehouse")
                .address("123 Main St")
                .capacity(1000)
                .availableSlots(500)
                .status(1)
                .build();

        user = User.builder()
                .id(1)
                .username("testuser")
                .password("encodedPassword")
                .fullName("Test User")
                .status((byte) 1)
                .warehouse(warehouse)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        loginResponse = LoginResponse.builder()
                .userId(1)
                .username("testuser")
                .fullName("Test User")
                .warehouseId(1)
                .accessToken("jwt-token")
                .authenticated(true)
                .build();
    }

    @Test
    @DisplayName("Login - Thành công")
    void login_Success() {
        // Given
        when(userRepository.findByUsernameWithWarehouse(anyString()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);
        when(jwtTokenConfig.generateToken(anyString(), anyInt(), anyInt()))
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

        // Verify event đã publish
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        UserLoginSuccessEvent publishedEvent = eventCaptor.getValue();
        assertEquals(1, publishedEvent.getUserId());
        assertEquals(1, publishedEvent.getWarehouseId());
        assertEquals("testuser", publishedEvent.getUsername());
    }

    @Test
    @DisplayName("Login - Tài khoản không tồn tại")
    void login_UserNotFound() {
        // Given
        when(userRepository.findByUsernameWithWarehouse(anyString()))
                .thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> authService.login(loginRequest));

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
        verify(userRepository).findByUsernameWithWarehouse("testuser");
        verifyNoInteractions(passwordEncoder, jwtTokenConfig, eventPublisher);
    }

    @Test
    @DisplayName("Login - Mật khẩu không đúng")
    void login_InvalidPassword() {
        // Given
        when(userRepository.findByUsernameWithWarehouse(anyString()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> authService.login(loginRequest));

        assertEquals(ErrorCode.INVALID_LOGIN_INFO, exception.getErrorCode());
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verifyNoInteractions(jwtTokenConfig, eventPublisher);
    }

//    @Test
//    @DisplayName("Login - Tài khoản bị vô hiệu hóa")
//    void login_UserDisabled() {
//        // Given
//        user.setStatus((byte) 0);
//        when(userRepository.findByUsernameWithWarehouse(anyString()))
//                .thenReturn(Optional.of(user));
//        when(passwordEncoder.matches(anyString(), anyString()))
//                .thenReturn(true);
//
//        // When & Then
//        AppException exception = assertThrows(AppException.class,
//                () -> authService.login(loginRequest));
//
//        assertEquals(ErrorCode.INVALID_LOGIN_INFO, exception.getErrorCode());
//    }

    @Test
    @DisplayName("Login - User không có warehouse")
    void login_UserWithoutWarehouse() {
        // Given
        user.setWarehouse(null);
        when(userRepository.findByUsernameWithWarehouse(anyString()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> authService.login(loginRequest));

        assertEquals(ErrorCode.INVALID_LOGIN_INFO, exception.getErrorCode());
    }

    @Test
    @DisplayName("Login - Warehouse không hợp lệ")
    void login_InvalidWarehouseId() {
        // Given
        when(userRepository.findByUsernameWithWarehouse(anyString()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);


        loginRequest.setWarehouseId(999);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> authService.login(loginRequest));

        assertEquals(ErrorCode.INVALID_LOGIN_INFO, exception.getErrorCode());
    }

    @Test
    @DisplayName("Login - Warehouse bị vô hiệu hóa")
    void login_WarehouseDisabled() {
        // Given
        warehouse.setStatus(0);
        when(userRepository.findByUsernameWithWarehouse(anyString()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> authService.login(loginRequest));

        assertEquals(ErrorCode.INVALID_LOGIN_INFO, exception.getErrorCode());
    }

    @Test
    @DisplayName("Login - Kiểm tra transactional")
    void login_Transactional() throws NoSuchMethodException {
        //  @Transactional annotation ton tai
        var method = AuthService.class.getMethod("login", LoginRequest.class);
        var transactional = method.getAnnotation(org.springframework.transaction.annotation.Transactional.class);

        assertNotNull(transactional, "Method should be @Transactional");
    }
}

