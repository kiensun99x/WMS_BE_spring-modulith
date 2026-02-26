package com.rk.WMS.auth;

import com.rk.WMS.auth.event.UserLoginSuccessEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for UserLoginSuccessEvent
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
        // Given - Chuẩn bị dữ liệu
        Object source = new Object();
        Long userId = 1L;
        Integer warehouseId = 100;
        String username = "testuser";

        // When - Tạo event
        UserLoginSuccessEvent event = new UserLoginSuccessEvent(
                source, userId, warehouseId, username
        );

        // Then - Kiểm tra các field
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
                source, 2L, 200, "admin"
        );

        // When & Then - Kiểm tra getter
        assertEquals(2L, event.getUserId());
        assertEquals(200, event.getWarehouseId());
        assertEquals("admin", event.getUsername());
    }
}