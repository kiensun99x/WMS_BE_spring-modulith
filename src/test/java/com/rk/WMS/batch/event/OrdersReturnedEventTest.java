package com.rk.WMS.batch.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Test class for OrdersReturnedEvent
 */
class OrdersReturnedEventTest {

    @Test
    @DisplayName("OrdersReturnedEvent - Khởi tạo thành công")
    void createEvent_Success() {
        // Given
        List<ReturnOrderPayload> orders = Arrays.asList(
                ReturnOrderPayload.builder().orderId(1L).build(),
                ReturnOrderPayload.builder().orderId(2L).build()
        );

        // When
        OrdersReturnedEvent event = new OrdersReturnedEvent(orders);

        // Then
        assertEquals(orders, event.getOrders());
        assertEquals(2, event.getOrders().size());
    }

    @Test
    @DisplayName("OrdersReturnedEvent - Orders rỗng")
    void createEvent_EmptyOrders() {
        // Given
        List<ReturnOrderPayload> orders = Arrays.asList();

        // When
        OrdersReturnedEvent event = new OrdersReturnedEvent(orders);

        // Then
        assertTrue(event.getOrders().isEmpty());
    }
}

