package com.rk.WMS.auth;


import com.rk.WMS.auth.event.UserLoginSuccessEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserLoginSuccessEventTest {

    @Test
    @DisplayName("Tạo UserLoginSuccessEvent - Thành công")
    void createUserLoginSuccessEvent_Success() {
        // Given
        Object source = new Object();
        Integer userId = 1;
        Integer warehouseId = 100;
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

    @Test
    @DisplayName("UserLoginSuccessEvent - Getter methods")
    void getterMethods_ReturnCorrectValues() {
        // Given
        Object source = new Object();
        UserLoginSuccessEvent event = new UserLoginSuccessEvent(
                source, 2, 200, "admin"
        );

        // When & Then
        assertEquals(2, event.getUserId());
        assertEquals(200, event.getWarehouseId());
        assertEquals("admin", event.getUsername());
    }
}
