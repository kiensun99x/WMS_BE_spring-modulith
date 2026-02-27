package com.rk.WMS.batch.service;


import com.rk.WMS.batch.service.Impl.DistanceCalculator;
import com.rk.WMS.batch.service.Impl.WarehouseSelector;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.warehouse.model.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Mục đích: Kiểm tra logic chọn warehouse gần nhất còn slot
 */
@ExtendWith(MockitoExtension.class)
class WarehouseSelectorTest {

    @Mock
    private DistanceCalculator distanceCalculator;

    @InjectMocks
    private WarehouseSelector warehouseSelector;

    private Order order;
    private List<Warehouse> warehouses;
    private Map<Long, Integer> remainingSlots;

    /**
     * Setup trước mỗi test case
     */
    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setReceiverLat(BigDecimal.valueOf(10.762622));
        order.setReceiverLon(BigDecimal.valueOf(106.660172));

        warehouses = Arrays.asList(
                createWarehouse(1L, "WH-001", 10.8, 106.7, 5),
                createWarehouse(2L, "WH-002", 10.9, 106.8, 3),
                createWarehouse(3L, "WH-003", 10.6, 106.5, 0) // Hết slot
        );

        remainingSlots = new HashMap<>();
        remainingSlots.put(1L, 5);
        remainingSlots.put(2L, 3);
        remainingSlots.put(3L, 0);
    }

    /**
     * Tạo đối tượng Warehouse cho test
     */
    private Warehouse createWarehouse(Long id, String code, double lat, double lon, int slots) {
        return Warehouse.builder()
                .warehouseId(id)
                .warehouseCode(code)
                .name("Warehouse " + id)
                .latitude(BigDecimal.valueOf(lat))
                .longitude(BigDecimal.valueOf(lon))
                .availableSlots(slots)
                .build();
    }

    /**
     * Test case: Chọn warehouse gần nhất còn slot
     */
    @Test
    @DisplayName("selectNearestWarehouseWithSlot - Chọn thành công")
    void selectNearestWarehouseWithSlot_Success() {
        // Given
        when(distanceCalculator.distanceKm(any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    BigDecimal lat1 = invocation.getArgument(0);
                    BigDecimal lon1 = invocation.getArgument(1);
                    BigDecimal lat2 = invocation.getArgument(2);
                    BigDecimal lon2 = invocation.getArgument(3);

                    // Giả lập khoảng cách: WH-001 gần nhất
                    if (lat2.equals(BigDecimal.valueOf(10.8))) {
                        return 5.0;
                    } else if (lat2.equals(BigDecimal.valueOf(10.9))) {
                        return 10.0;
                    }
                    return 100.0;
                });

        // When
        Warehouse selected = warehouseSelector.selectNearestWarehouseWithSlot(
                order, warehouses, remainingSlots);

        // Then
        assertNotNull(selected);
        assertEquals(1L, selected.getWarehouseId());
        verify(distanceCalculator, times(2)).distanceKm(any(), any(), any(), any());
    }

    /**
     * Test case: Chọn warehouse khi tất cả đều hết slot
     */
    @Test
    @DisplayName("selectNearestWarehouseWithSlot - Tất cả hết slot")
    void selectNearestWarehouseWithSlot_AllOutOfSlots() {
        // Given
        remainingSlots.put(1L, 0);
        remainingSlots.put(2L, 0);

        // When
        Warehouse selected = warehouseSelector.selectNearestWarehouseWithSlot(
                order, warehouses, remainingSlots);

        // Then
        assertNull(selected);
        verify(distanceCalculator, never()).distanceKm(any(), any(), any(), any());
    }

    /**
     * Test case: Chọn warehouse với khoảng cách bằng nhau
     */
    @Test
    @DisplayName("selectNearestWarehouseWithSlot - Khoảng cách bằng nhau")
    void selectNearestWarehouseWithSlot_EqualDistance() {
        // Given
        when(distanceCalculator.distanceKm(any(), any(), any(), any()))
                .thenReturn(10.0); // Cả 2 đều cách 10km

        // When
        Warehouse selected = warehouseSelector.selectNearestWarehouseWithSlot(
                order, warehouses, remainingSlots);

        // Then - Chọn warehouse đầu tiên trong stream
        assertNotNull(selected);
        assertTrue(selected.getWarehouseId() == 1L || selected.getWarehouseId() == 2L);
    }

    /**
     * Test case: Chọn warehouse với danh sách rỗng
     */
    @Test
    @DisplayName("selectNearestWarehouseWithSlot - Danh sách warehouse rỗng")
    void selectNearestWarehouseWithSlot_EmptyWarehouses() {
        // When
        Warehouse selected = warehouseSelector.selectNearestWarehouseWithSlot(
                order, Collections.emptyList(), remainingSlots);

        // Then
        assertNull(selected);
    }

    /**
     * Test case: Chọn warehouse với remainingSlots rỗng
     */
    @Test
    @DisplayName("selectNearestWarehouseWithSlot - remainingSlots rỗng")
    void selectNearestWarehouseWithSlot_EmptyRemainingSlots() {
        // Given
        remainingSlots.clear();

        // When
        Warehouse selected = warehouseSelector.selectNearestWarehouseWithSlot(
                order, warehouses, remainingSlots);

        // Then - Không warehouse nào còn slot (mặc định 0)
        assertNull(selected);
    }

    /**
     * Test case: Chọn warehouse với warehouse không có trong remainingSlots
     */
    @Test
    @DisplayName("selectNearestWarehouseWithSlot - Warehouse không có trong remainingSlots")
    void selectNearestWarehouseWithSlot_WarehouseNotInRemainingSlots() {
        // Given
        remainingSlots.clear();
        remainingSlots.put(4L, 5); // Warehouse 4L không tồn tại

        // When
        Warehouse selected = warehouseSelector.selectNearestWarehouseWithSlot(
                order, warehouses, remainingSlots);

        // Then - Không warehouse nào được chọn
        assertNull(selected);
    }
}
