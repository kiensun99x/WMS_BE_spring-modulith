package com.rk.WMS.auth;

import com.rk.WMS.auth.dto.response.LoginResponse;
import com.rk.WMS.auth.mapper.AuthMapper;
import com.rk.WMS.auth.model.User;
import com.rk.WMS.common.constants.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mục đích: Kiểm tra việc mapping giữa Entity và DTO
 */
class AuthMapperTest {

    private AuthMapper authMapper = Mappers.getMapper(AuthMapper.class);  // Khởi tạo mapper

    private User user;

    /**
     * Setup trước mỗi test case
     * Tạo đối tượng User mẫu để test
     */
    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(100L)
                .username("testuser")
                .password("encodedPass")
                .fullName("Test User Full")
                .status(UserStatus.ACTIVE)
                .warehouse(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Test case: Map User thành công sang LoginResponse
     * Kiểm tra tất cả các field được map chính xác
     * Expected:
     * - userId = user.id
     * - username = user.username
     * - fullName = user.fullName
     * - warehouseId = user.warehouse
     */
    @Test
    @DisplayName("Map User to LoginResponse - Thành công")
    void toLoginResponse_Success() {
        // When - Thực hiện mapping
        LoginResponse response = authMapper.toLoginResponse(user);

        // Then - Kiểm tra kết quả
        assertNotNull(response);
        assertEquals(100L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("Test User Full", response.getFullName());
        assertEquals(1, response.getWarehouseId());
    }

    /**
     * Test case: Map User không có warehouse
     * Kiểm tra xử lý trường hợp warehouse = null
     * Expected: warehouseId = null
     */
    @Test
    @DisplayName("Map User to LoginResponse - User không có warehouse")
    void toLoginResponse_UserWithoutWarehouse() {
        // Given - User không có warehouse
        user.setWarehouse(null);

        // When
        LoginResponse response = authMapper.toLoginResponse(user);

        // Then
        assertNotNull(response);
        assertEquals(100L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("Test User Full", response.getFullName());
        assertNull(response.getWarehouseId());
    }

    /**
     * Test case: Map User null
     * Kiểm tra xử lý khi input là null
     * Expected: response = null
     */
    @Test
    @DisplayName("Map User to LoginResponse - Null user")
    void toLoginResponse_NullUser() {
        // When
        LoginResponse response = authMapper.toLoginResponse(null);

        // Then
        assertNull(response);
    }
}