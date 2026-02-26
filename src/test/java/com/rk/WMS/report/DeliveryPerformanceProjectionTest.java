package com.rk.WMS.report;


import com.rk.WMS.report.projection.DeliveryPerformanceProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mục đích: Kiểm tra việc implement projection interface
 */
class DeliveryPerformanceProjectionTest {

    /**
     * Test case: Implement projection với Anonymous class
     * Expected: Các method trả về đúng giá trị đã set
     */
    @Test
    @DisplayName("Anonymous class - Implement projection thành công")
    void anonymousClass_ImplementProjection_Success() {
        // Given
        Long expectedWarehouseId = 100L;
        LocalDateTime expectedCreatedAt = LocalDateTime.now();
        Integer expectedStatus = 3; // DELIVERED
        Long expectedReasonId = 101L;
        Long expectedTotal = 50L;

        // When
        DeliveryPerformanceProjection projection = new DeliveryPerformanceProjection() {
            @Override
            public Long getWarehouseId() {
                return expectedWarehouseId;
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return expectedCreatedAt;
            }

            @Override
            public Integer getStatus() {
                return expectedStatus;
            }

            @Override
            public Long getFailureReasonId() {
                return expectedReasonId;
            }

            @Override
            public Long getTotal() {
                return expectedTotal;
            }
        };

        // Then
        assertEquals(expectedWarehouseId, projection.getWarehouseId());
        assertEquals(expectedCreatedAt, projection.getCreatedAt());
        assertEquals(expectedStatus, projection.getStatus());
        assertEquals(expectedReasonId, projection.getFailureReasonId());
        assertEquals(expectedTotal, projection.getTotal());
    }

    /**
     * Test case: FailureReasonId có thể null (đơn thành công)
     */
    @Test
    @DisplayName("FailureReasonId - Có thể null")
    void failureReasonId_CanBeNull() {
        // Given
        DeliveryPerformanceProjection projection = new DeliveryPerformanceProjection() {
            @Override
            public Long getWarehouseId() {
                return 1L;
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return LocalDateTime.now();
            }

            @Override
            public Integer getStatus() {
                return 3; // DELIVERED
            }

            @Override
            public Long getFailureReasonId() {
                return null;
            }

            @Override
            public Long getTotal() {
                return 10L;
            }
        };

        // Then
        assertNull(projection.getFailureReasonId());
    }

    /**
     * Test case: Total có thể là 0
     */
    @Test
    @DisplayName("Total - Có thể bằng 0")
    void total_CanBeZero() {
        // Given
        DeliveryPerformanceProjection projection = new DeliveryPerformanceProjection() {
            @Override
            public Long getWarehouseId() {
                return 1L;
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return LocalDateTime.now();
            }

            @Override
            public Integer getStatus() {
                return 4; // FAILED
            }

            @Override
            public Long getFailureReasonId() {
                return 101L;
            }

            @Override
            public Long getTotal() {
                return 0L;
            }
        };

        // Then
        assertEquals(0L, projection.getTotal());
    }
}
