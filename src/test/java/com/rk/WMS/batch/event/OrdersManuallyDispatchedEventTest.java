package com.rk.WMS.batch.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for OrdersManuallyDispatchedEvent
 */
class OrdersManuallyDispatchedEventTest {

    @Test
    @DisplayName("OrdersManuallyDispatchedEvent - Khởi tạo thành công")
    void createEvent_Success() {
        // Given
        List<Long> orderIds = Arrays.asList(1L, 2L, 3L);
        Long warehouseId = 100L;
        LocalDateTime now = LocalDateTime.now();

        // When
        OrdersManuallyDispatchedEvent event = new OrdersManuallyDispatchedEvent(orderIds, warehouseId, now);

        // Then
        assertEquals(orderIds, event.getOrderIds());
        assertEquals(warehouseId, event.getWarehouseId());
        assertEquals(now, event.getDispatchAt());
        assertEquals(3, event.getOrderIds().size());
    }

    @Test
    @DisplayName("OrdersManuallyDispatchedEvent - OrderIds rỗng")
    void createEvent_EmptyOrderIds() {
        // Given
        List<Long> orderIds = Arrays.asList();
        Long warehouseId = 100L;
        LocalDateTime now = LocalDateTime.now();

        // When
        OrdersManuallyDispatchedEvent event = new OrdersManuallyDispatchedEvent(orderIds, warehouseId, now);

        // Then
        assertTrue(event.getOrderIds().isEmpty());
    }
}