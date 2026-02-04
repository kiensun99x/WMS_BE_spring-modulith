package com.rk.WMS.auth;

import com.rk.WMS.auth.dto.response.LoginResponse;
import com.rk.WMS.auth.mapper.AuthMapper;
import com.rk.WMS.auth.model.User;
import com.rk.WMS.warehouse.model.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuthMapperTest {

    private AuthMapper authMapper = Mappers.getMapper(AuthMapper.class);

    private User user;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        warehouse = Warehouse.builder()
                .warehouseId(1)
                .warehouseCode("WH-001")
                .name("Main Warehouse")
                .build();

        user = User.builder()
                .id(100)
                .username("testuser")
                .password("encodedPass")
                .fullName("Test User Full")
                .status((byte) 1)
                .warehouse(warehouse)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Map User to LoginResponse - Thành công")
    void toLoginResponse_Success() {
        // When
        LoginResponse response = authMapper.toLoginResponse(user);

        // Then
        assertNotNull(response);
        assertEquals(100, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("Test User Full", response.getFullName());
        assertEquals(1, response.getWarehouseId());
    }

    @Test
    @DisplayName("Map User to LoginResponse - User không có warehouse")
    void toLoginResponse_UserWithoutWarehouse() {
        // Given
        user.setWarehouse(null);

        // When
        LoginResponse response = authMapper.toLoginResponse(user);

        // Then
        assertNotNull(response);
        assertEquals(100, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertNull(response.getWarehouseId());
    }

    @Test
    @DisplayName("Map User to LoginResponse - Null user")
    void toLoginResponse_NullUser() {
        // When
        LoginResponse response = authMapper.toLoginResponse(null);

        // Then
        assertNull(response);
    }
}
