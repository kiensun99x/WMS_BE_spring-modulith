package com.rk.WMS.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rk.WMS.auth.controller.AuthController;
import com.rk.WMS.auth.dto.request.LoginRequest;
import com.rk.WMS.auth.dto.response.LoginResponse;
import com.rk.WMS.auth.service.AuthService;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.common.exception.GlobalExceptionHandler;
import com.rk.WMS.common.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

//    @BeforeEach
//    void setUp() {
//        objectMapper = new ObjectMapper();
//        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
//    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(globalExceptionHandler)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Thành công")
    void login_Success() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "password123", 1);
        LoginResponse loginResponse = LoginResponse.builder()
                .userId(1)
                .username("testUser")
                .fullName("Test User")
                .warehouseId(1)
                .accessToken("jwt-token")
                .authenticated(true)
                .build();

        ApiResponse<LoginResponse> expectedResponse = ApiResponse.<LoginResponse>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message("Đăng nhập thành công")
                .result(loginResponse)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(expectedResponse.getCode()))
                .andExpect(jsonPath("$.message").value(expectedResponse.getMessage()))
                .andExpect(jsonPath("$.result.userId").value(loginResponse.getUserId()))
                .andExpect(jsonPath("$.result.username").value(loginResponse.getUsername()))
                .andExpect(jsonPath("$.result.fullName").value(loginResponse.getFullName()))
                .andExpect(jsonPath("$.result.warehouseId").value(loginResponse.getWarehouseId()))
                .andExpect(jsonPath("$.result.accessToken").value(loginResponse.getAccessToken()))
                .andExpect(jsonPath("$.result.authenticated").value(loginResponse.isAuthenticated()));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Thất bại do không tìm thấy tài khoản")
    void login_Failed_AccountNotFound() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("nonExistentUser", "password123", 1);

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCOUNT_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").exists());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Thất bại do thông tin đăng nhập không chính xác")
    void login_Failed_InvalidLoginInfo() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "wrongPassword", 1);

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AppException(ErrorCode.INVALID_LOGIN_INFO));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_LOGIN_INFO.getCode()))
                .andExpect(jsonPath("$.message").exists());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }


    @ParameterizedTest
    @MethodSource("provideInvalidLoginRequests")
    @DisplayName("POST /api/v1/auth/login - Validation failed")
    void login_ValidationFailed(LoginRequest request, String expectedField) throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value(containsString(expectedField)));

        verify(authService, never()).login(any(LoginRequest.class));
    }

    private static Stream<Arguments> provideInvalidLoginRequests() {
        return Stream.of(
                Arguments.of(new LoginRequest("", "password123", 1), "username"),
                Arguments.of(new LoginRequest("   ", "password123", 1), "username"),
                Arguments.of(new LoginRequest("testUser", "", 1), "password"),
                Arguments.of(new LoginRequest("testUser", "   ", 1), "password"),
                Arguments.of(new LoginRequest("testUser", "password123", null), "warehouseId"),
                Arguments.of(new LoginRequest(null, "password123", 1), "username"),
                Arguments.of(new LoginRequest("testUser", null, 1), "password")
        );
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Request body là null")
    void login_NullRequestBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, never()).login(any(LoginRequest.class));
    }


    @Test
    @DisplayName("POST /api/v1/auth/login - Kiểm tra logging khi thành công")
    void login_VerifySuccessLogging() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "password123", 1);
        LoginResponse loginResponse = LoginResponse.builder()
                .userId(1)
                .username("testUser")
                .fullName("Test User")
                .warehouseId(1)
                .accessToken("jwt-token")
                .authenticated(true)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService, times(1)).login(argThat(loginReq ->
                loginReq.getUsername().equals("testUser") &&
                        loginReq.getWarehouseId() == 1));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Kiểm tra logging khi thất bại")
    void login_VerifyFailureLogging() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "wrongPassword", 1);

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AppException(ErrorCode.INVALID_LOGIN_INFO));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authService, times(1)).login(argThat(loginReq ->
                loginReq.getUsername().equals("testUser") &&
                        loginReq.getWarehouseId() == 1));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Trả về đúng cấu trúc ApiResponse")
    void login_ReturnsCorrectApiResponseStructure() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "password123", 1);
        LoginResponse loginResponse = LoginResponse.builder()
                .userId(1)
                .username("testUser")
                .fullName("Test User")
                .warehouseId(1)
                .accessToken("jwt-token")
                .authenticated(true)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.result.userId").exists())
                .andExpect(jsonPath("$.result.username").exists())
                .andExpect(jsonPath("$.result.fullName").exists())
                .andExpect(jsonPath("$.result.warehouseId").exists())
                .andExpect(jsonPath("$.result.accessToken").exists())
                .andExpect(jsonPath("$.result.authenticated").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Kiểm tra mã lỗi và message khi thành công")
    void login_VerifySuccessCodeAndMessage() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "password123", 1);
        LoginResponse loginResponse = LoginResponse.builder()
                .userId(1)
                .username("testUser")
                .fullName("Test User")
                .warehouseId(1)
                .accessToken("jwt-token")
                .authenticated(true)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SYSS-0001")) // SUCCESS code
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - WarehouseId là số âm (validation failed)")
    void login_WarehouseIdNegative() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "password123", -1);

        // Act & Assert - Bây giờ sẽ bắt được lỗi validation
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value(containsString("WarehouseId phải lớn hơn 0")));

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - WarehouseId bằng 0 (validation failed)")
    void login_WarehouseIdZero() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "password123", 0);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value(containsString("WarehouseId phải lớn hơn 0")));

        verify(authService, never()).login(any(LoginRequest.class));
    }


}