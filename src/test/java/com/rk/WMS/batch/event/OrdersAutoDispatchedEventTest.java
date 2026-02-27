package com.rk.WMS.batch.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for OrdersAutoDispatchedEvent
 */
class OrdersAutoDispatchedEventTest {

    @Test
    @DisplayName("OrdersAutoDispatchedEvent - Khởi tạo thành công")
    void createEvent_Success() {
        // Given
        Map<Long, Long> orderWarehouseMap = new HashMap<>();
        orderWarehouseMap.put(1L, 100L);
        orderWarehouseMap.put(2L, 100L);
        LocalDateTime now = LocalDateTime.now();

        // When
        OrdersAutoDispatchedEvent event = new OrdersAutoDispatchedEvent(orderWarehouseMap, now);

        // Then
        assertEquals(orderWarehouseMap, event.getOrderWarehouseMap());
        assertEquals(now, event.getDispatchAt());
        assertEquals(2, event.getOrderWarehouseMap().size());
    }

    @Test
    @DisplayName("OrdersAutoDispatchedEvent - Map rỗng")
    void createEvent_EmptyMap() {
        // Given
        Map<Long, Long> orderWarehouseMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // When
        OrdersAutoDispatchedEvent event = new OrdersAutoDispatchedEvent(orderWarehouseMap, now);

        // Then
        assertTrue(event.getOrderWarehouseMap().isEmpty());
    }
}

