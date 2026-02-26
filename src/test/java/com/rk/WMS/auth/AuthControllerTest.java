package com.rk.WMS.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rk.WMS.auth.controller.AuthController;
import com.rk.WMS.auth.dto.request.LoginRequest;
import com.rk.WMS.auth.dto.response.LoginResponse;
import com.rk.WMS.auth.service.AuthService;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.common.exception.GlobalExceptionHandler;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Mục đích: Kiểm tra các API endpoint liên quan đến xác thực người dùng
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;  // Mock service layer

    @InjectMocks
    private AuthController authController;  // Inject mock vào controller

    private ObjectMapper objectMapper;  // Chuyển đổi object <-> JSON

    /**
     * Setup trước mỗi test case
     * Khởi tạo MockMvc với controller và global exception handler
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(globalExceptionHandler)  // Bắt exception global
                .build();
    }

    /**
     * Test case: Đăng nhập thành công
     * Expected:
     * - HTTP Status 200 (OK)
     * - Response đúng cấu trúc ApiResponse
     * - Code = SUCCESS
     * - Message = "Đăng nhập thành công"
     * - Result chứa thông tin user và token
     */
    @Test
    @DisplayName("POST /auth/login - Thành công")
    void login_Success() throws Exception {
        // Arrange - Chuẩn bị dữ liệu test
        LoginRequest request = new LoginRequest("testUser", "password123", 1);
        LoginResponse loginResponse = LoginResponse.builder()
                .userId(1L)
                .username("testUser")
                .fullName("Test User")
                .warehouseId(1)
                .accessToken("jwt-token")
                .authenticated(true)
                .build();

        // Mock behavior
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                .andExpect(jsonPath("$.result.userId").value(loginResponse.getUserId()))
                .andExpect(jsonPath("$.result.username").value(loginResponse.getUsername()))
                .andExpect(jsonPath("$.result.fullName").value(loginResponse.getFullName()))
                .andExpect(jsonPath("$.result.warehouseId").value(loginResponse.getWarehouseId()))
                .andExpect(jsonPath("$.result.accessToken").value(loginResponse.getAccessToken()))
                .andExpect(jsonPath("$.result.authenticated").value(loginResponse.isAuthenticated()));

        // Verify service được gọi đúng 1 lần
        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test case: Đăng nhập thất bại - Không tìm thấy tài khoản
     * Expected:
     * - HTTP Status 404 (Not Found)
     * - Code = ACCOUNT_NOT_FOUND
     */
    @Test
    @DisplayName("POST /auth/login - Thất bại do không tìm thấy tài khoản")
    void login_Failed_AccountNotFound() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("nonExistentUser", "password123", 1);

        // Mock service throw exception
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCOUNT_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").exists());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test case: Đăng nhập thất bại - Thông tin đăng nhập không chính xác
     * (sai mật khẩu, tài khoản inactive, sai warehouse)
     * Expected:
     * - HTTP Status 401 (Unauthorized)
     * - Code = INVALID_LOGIN_INFO
     */
    @Test
    @DisplayName("POST /auth/login - Thất bại do thông tin đăng nhập không chính xác")
    void login_Failed_InvalidLoginInfo() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "wrongPassword", 1);

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AppException(ErrorCode.INVALID_LOGIN_INFO));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_LOGIN_INFO.getCode()))
                .andExpect(jsonPath("$.message").exists());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Parameterized Test: Kiểm tra validation cho các trường dữ liệu
     * Sử dụng MethodSource để cung cấp các test data khác nhau
     * Expected:
     * - HTTP Status 400 (Bad Request)
     * - Code = VALIDATION_ERROR
     * - Message chứa tên field bị lỗi
     */
    @ParameterizedTest
    @MethodSource("provideInvalidLoginRequests")
    @DisplayName("POST /auth/login - Validation failed")
    void login_ValidationFailed(LoginRequest request, String expectedField) throws Exception {
        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value(containsString(expectedField)));

        // Verify service không được gọi do validation fail
        verify(authService, never()).login(any(LoginRequest.class));
    }

    /**
     * Cung cấp dữ liệu test cho ParameterizedTest
     * Bao gồm các trường hợp: username trống, password trống, warehouseId null
     */
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

    /**
     * Test case: Request body là null hoặc empty
     * Expected: HTTP Status 400 (Bad Request)
     */
    @Test
    @DisplayName("POST /auth/login - Request body là null")
    void login_NullRequestBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    /**
     * Test case: Kiểm tra logging khi đăng nhập thành công
     * Đảm bảo service nhận đúng tham số
     */
    @Test
    @DisplayName("POST /auth/login - Kiểm tra logging khi thành công")
    void login_VerifySuccessLogging() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "password123", 1);
        LoginResponse loginResponse = LoginResponse.builder()
                .userId(1L)
                .username("testUser")
                .fullName("Test User")
                .warehouseId(1)
                .accessToken("jwt-token")
                .authenticated(true)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify tham số đúng
        verify(authService, times(1)).login(argThat(loginReq ->
                loginReq.getUsername().equals("testUser") &&
                        loginReq.getWarehouseId() == 1));
    }

    /**
     * Test case: Kiểm tra logging khi đăng nhập thất bại
     * Đảm bảo service nhận đúng tham số trước khi throw exception
     */
    @Test
    @DisplayName("POST /auth/login - Kiểm tra logging khi thất bại")
    void login_VerifyFailureLogging() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "wrongPassword", 1);

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AppException(ErrorCode.INVALID_LOGIN_INFO));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authService, times(1)).login(argThat(loginReq ->
                loginReq.getUsername().equals("testUser") &&
                        loginReq.getWarehouseId() == 1));
    }

    /**
     * Test case: Kiểm tra cấu trúc response khi thành công
     * Đảm bảo tất cả các field đều tồn tại
     */
    @Test
    @DisplayName("POST /auth/login - Trả về đúng cấu trúc ApiResponse")
    void login_ReturnsCorrectApiResponseStructure() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "password123", 1);
        LoginResponse loginResponse = LoginResponse.builder()
                .userId(1L)
                .username("testUser")
                .fullName("Test User")
                .warehouseId(1)
                .accessToken("jwt-token")
                .authenticated(true)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
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

    /**
     * Test case: Kiểm tra mã lỗi và message khi thành công
     * Đảm bảo format đúng theo chuẩn
     */
    @Test
    @DisplayName("POST /auth/login - Kiểm tra mã lỗi và message khi thành công")
    void login_VerifySuccessCodeAndMessage() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "password123", 1);
        LoginResponse loginResponse = LoginResponse.builder()
                .userId(1L)
                .username("testUser")
                .fullName("Test User")
                .warehouseId(1)
                .accessToken("jwt-token")
                .authenticated(true)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test case: WarehouseId là số âm
     * Kiểm tra @Min validation
     */
    @Test
    @DisplayName("POST /auth/login - WarehouseId là số âm (validation failed)")
    void login_WarehouseIdNegative() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "password123", -1);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value(containsString("WarehouseId phải lớn hơn 0")));

        verify(authService, never()).login(any(LoginRequest.class));
    }

    /**
     * Test case: WarehouseId bằng 0
     * Kiểm tra @Min validation với giá trị biên
     */
    @Test
    @DisplayName("POST /auth/login - WarehouseId bằng 0 (validation failed)")
    void login_WarehouseIdZero() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "password123", 0);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value(containsString("WarehouseId phải lớn hơn 0")));

        verify(authService, never()).login(any(LoginRequest.class));
    }
}