package com.rk.WMS.auth;

import com.rk.WMS.auth.event.UserLoginSuccessEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mục đích: Kiểm tra việc tạo và lấy dữ liệu từ event
 * Event được publish khi user đăng nhập thành công
 */
class UserLoginSuccessEventTest {

    /**
     * Test case: Tạo event thành công với đầy đủ thông tin
     * Kiểm tra constructor khởi tạo đúng các field
     */
    @Test
    @DisplayName("Tạo UserLoginSuccessEvent - Thành công")
    void createUserLoginSuccessEvent_Success() {
        // Given
        Object source = new Object();
        Long userId = 1L;
        Long warehouseId = 100L;
        String username = "testuser";

        // When
        UserLoginSuccessEvent event = new UserLoginSuccessEvent(
                source, userId, warehouseId, username
        );

        // Then
        assertEquals(source, event.getSource());
        assertEquals(userId, event.getUserId());
        assertEquals(warehouseId, event.getWarehouseId());
        assertEquals(username, event.getUsername());
    }

    /**
     * Test case: Kiểm tra các getter methods
     * Đảm bảo có thể lấy đúng giá trị đã set
     */
    @Test
    @DisplayName("UserLoginSuccessEvent - Getter methods")
    void getterMethods_ReturnCorrectValues() {
        // Given
        Object source = new Object();
        UserLoginSuccessEvent event = new UserLoginSuccessEvent(
                source, 2L, 200L, "admin"
        );

        // When & Then
        assertEquals(2L, event.getUserId());
        assertEquals(200L, event.getWarehouseId());
        assertEquals("admin", event.getUsername());
    }
}