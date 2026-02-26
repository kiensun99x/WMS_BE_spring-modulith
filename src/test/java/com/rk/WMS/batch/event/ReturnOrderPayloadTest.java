package com.rk.WMS.batch.event;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for ReturnOrderPayload
 */
class ReturnOrderPayloadTest {

    @Test
    @DisplayName("ReturnOrderPayload - Builder khởi tạo thành công")
    void builder_CreatePayload_Success() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        ReturnOrderPayload payload = ReturnOrderPayload.builder()
                .orderId(1L)
                .orderCode("ORD-001")
                .warehouseId(100L)
                .supplierName("Supplier A")
                .supplierEmail("supplier@test.com")
                .receiverName("Receiver B")
                .receiverEmail("receiver@test.com")
                .failedDeliveryCount(3)
                .actor("system")
                .eventTime(now)
                .build();

        // Then
        assertEquals(1L, payload.getOrderId());
        assertEquals("ORD-001", payload.getOrderCode());
        assertEquals(100L, payload.getWarehouseId());
        assertEquals("Supplier A", payload.getSupplierName());
        assertEquals("supplier@test.com", payload.getSupplierEmail());
        assertEquals("Receiver B", payload.getReceiverName());
        assertEquals("receiver@test.com", payload.getReceiverEmail());
        assertEquals(3, payload.getFailedDeliveryCount());
        assertEquals("system", payload.getActor());
        assertEquals(now, payload.getEventTime());
    }

    @Test
    @DisplayName("ReturnOrderPayload - AllArgsConstructor")
    void allArgsConstructor_CreatePayload_Success() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        ReturnOrderPayload payload = new ReturnOrderPayload(
                1L, "ORD-001", 100L, "Supplier A", "supplier@test.com",
                "Receiver B", "receiver@test.com", 3, "system", now
        );

        // Then
        assertEquals(1L, payload.getOrderId());
        assertEquals("ORD-001", payload.getOrderCode());
        assertEquals(100L, payload.getWarehouseId());
        assertEquals("Supplier A", payload.getSupplierName());
        assertEquals("supplier@test.com", payload.getSupplierEmail());
        assertEquals("Receiver B", payload.getReceiverName());
        assertEquals("receiver@test.com", payload.getReceiverEmail());
        assertEquals(3, payload.getFailedDeliveryCount());
        assertEquals("system", payload.getActor());
        assertEquals(now, payload.getEventTime());
    }
}
