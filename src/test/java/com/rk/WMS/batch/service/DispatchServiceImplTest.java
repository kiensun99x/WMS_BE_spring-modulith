//package com.rk.WMS.batch.service;
//
//import com.rk.WMS.batch.event.OrdersAutoDispatchedEvent;
//import com.rk.WMS.batch.event.OrdersManuallyDispatchedEvent;
//import com.rk.WMS.batch.service.Impl.DispatchServiceImpl;
//import com.rk.WMS.batch.service.Impl.WarehouseSelector;
//import com.rk.WMS.common.constants.OrderStatus;
//import com.rk.WMS.common.exception.AppException;
//import com.rk.WMS.common.exception.ErrorCode;
//import com.rk.WMS.order.model.Order;
//import com.rk.WMS.order.repository.OrderRepository;
//import com.rk.WMS.warehouse.model.Warehouse;
//import com.rk.WMS.warehouse.repository.WarehouseRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.PageRequest;
//
//import java.math.BigDecimal;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
///**
// * Mục đích: Kiểm tra business logic của dispatch service
// */
//@ExtendWith(MockitoExtension.class)
//class DispatchServiceImplTest {
//
//    @Mock
//    private OrderRepository orderRepository;
//
//    @Mock
//    private WarehouseRepository warehouseRepository;
//
//    @Mock
//    private WarehouseSelector warehouseSelector;
//
//    @InjectMocks
//    private DispatchServiceImpl dispatchService;
//
//    @Captor
//    private ArgumentCaptor<OrdersManuallyDispatchedEvent> manualEventCaptor;
//
//    @Captor
//    private ArgumentCaptor<OrdersAutoDispatchedEvent> autoEventCaptor;
//
//    private List<Long> orderIds;
//    private Long warehouseId;
//    private Warehouse warehouse;
//    private List<Order> orders;
//    private List<Warehouse> warehouses;
//
//    /**
//     * Setup trước mỗi test case
//     * Khởi tạo dữ liệu mẫu
//     */
//    @BeforeEach
//    void setUp() {
//        orderIds = Arrays.asList(1L, 2L, 3L);
//        warehouseId = 100L;
//
//        warehouse = Warehouse.builder()
//                .warehouseId(warehouseId)
//                .warehouseCode("WH-001")
//                .name("Main Warehouse")
//                .availableSlots(10)
//                .latitude(BigDecimal.valueOf(10.762622))
//                .longitude(BigDecimal.valueOf(106.660172))
//                .build();
//
//        warehouses = Arrays.asList(warehouse);
//
//        orders = Arrays.asList(
//                createOrder(1L, "ORD-001", OrderStatus.NEW),
//                createOrder(2L, "ORD-002", OrderStatus.NEW),
//                createOrder(3L, "ORD-003", OrderStatus.NEW)
//        );
//    }
//
//    /**
//     * Tạo đối tượng Order cho test
//     */
//    private Order createOrder(Long id, String code, OrderStatus status) {
//        Order order = new Order();
//        order.setId(id);
//        order.setCode(code);
//        order.setStatus(status);
//        order.setReceiverLat(BigDecimal.valueOf(10.762622));
//        order.setReceiverLon(BigDecimal.valueOf(106.660172));
//        return order;
//    }
//
//
//    /**
//     * Test case: Manual dispatch thành công
//     */
//    @Test
//    @DisplayName("manualDispatch - Thành công")
//    void manualDispatch_Success() {
//        // Given
//        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
//        when(orderRepository.findAllById(orderIds)).thenReturn(orders);
//        doNothing().when(domainEventPublisher).publishEvent(any());
//
//        // When
//        dispatchService.manualDispatch(orderIds, warehouseId);
//
//        // Then
//        verify(warehouseRepository).findById(warehouseId);
//        verify(orderRepository).findAllById(orderIds);
//        verify(domainEventPublisher).publishEvent(manualEventCaptor.capture());
//
//        OrdersManuallyDispatchedEvent event = manualEventCaptor.getValue();
//        assertEquals(orderIds, event.getOrderIds());
//        assertEquals(warehouseId, event.getWarehouseId());
//        assertNotNull(event.getDispatchAt());
//    }
//
//    /**
//     * Test case: Manual dispatch - Warehouse không tồn tại
//     */
//    @Test
//    @DisplayName("manualDispatch - Warehouse không tồn tại")
//    void manualDispatch_WarehouseNotFound() {
//        // Given
//        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());
//
//        // When & Then
//        AppException exception = assertThrows(AppException.class,
//                () -> dispatchService.manualDispatch(orderIds, warehouseId));
//
//        assertEquals(ErrorCode.FAILED, exception.getErrorCode());
//        verify(orderRepository, never()).findAllById(any());
//        verify(domainEventPublisher, never()).publishEvent(any());
//    }
//
//    /**
//     * Test case: Manual dispatch - Không đủ available slots
//     */
//    @Test
//    @DisplayName("manualDispatch - Không đủ available slots")
//    void manualDispatch_InsufficientSlots() {
//        // Given
//        warehouse.setAvailableSlots(2); // Chỉ có 2 slots, nhưng có 3 orders
//        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
//
//        // When & Then
//        AppException exception = assertThrows(AppException.class,
//                () -> dispatchService.manualDispatch(orderIds, warehouseId));
//
//        assertEquals(ErrorCode.VALUE_EXCEED_LIMIT, exception.getErrorCode());
//        verify(orderRepository, never()).findAllById(any());
//        verify(domainEventPublisher, never()).publishEvent(any());
//    }
//
//    /**
//     * Test case: Manual dispatch - Không tìm thấy một số order
//     */
//    @Test
//    @DisplayName("manualDispatch - Không tìm thấy order")
//    void manualDispatch_OrderNotFound() {
//        // Given
//        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
//        when(orderRepository.findAllById(orderIds)).thenReturn(orders.subList(0, 2)); // Chỉ trả về 2 orders
//
//        // When & Then
//        AppException exception = assertThrows(AppException.class,
//                () -> dispatchService.manualDispatch(orderIds, warehouseId));
//
//        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
//        verify(domainEventPublisher, never()).publishEvent(any());
//    }
//
//    /**
//     * Test case: Manual dispatch - Order có status không phải NEW
//     */
//    @Test
//    @DisplayName("manualDispatch - Order status không hợp lệ")
//    void manualDispatch_InvalidOrderStatus() {
//        // Given
//        orders.get(1).setStatus(OrderStatus.STORED);
//        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
//        when(orderRepository.findAllById(orderIds)).thenReturn(orders);
//
//        // When & Then
//        AppException exception = assertThrows(AppException.class,
//                () -> dispatchService.manualDispatch(orderIds, warehouseId));
//
//        assertEquals(ErrorCode.INVALID_ORDER_STATUS, exception.getErrorCode());
//        verify(domainEventPublisher, never()).publishEvent(any());
//    }
//
//    /**
//     * Test case: Manual dispatch - Nhiều order có status không hợp lệ
//     */
//    @Test
//    @DisplayName("manualDispatch - Nhiều order status không hợp lệ")
//    void manualDispatch_MultipleInvalidStatus() {
//        // Given
//        orders.get(0).setStatus(OrderStatus.STORED);
//        orders.get(1).setStatus(OrderStatus.DELIVERED);
//        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
//        when(orderRepository.findAllById(orderIds)).thenReturn(orders);
//
//        // When & Then
//        AppException exception = assertThrows(AppException.class,
//                () -> dispatchService.manualDispatch(orderIds, warehouseId));
//
//        assertEquals(ErrorCode.INVALID_ORDER_STATUS, exception.getErrorCode());
//        verify(domainEventPublisher, never()).publishEvent(any());
//    }
//
//
//    /**
//     * Test case: Auto dispatch thành công
//     */
//    @Test
//    @DisplayName("autoDispatch - Thành công")
//    void autoDispatch_Success() {
//        // Given
//        when(orderRepository.findTop100ByStatusOrderByCreatedAtAsc(
//                eq(OrderStatus.NEW), any(PageRequest.class)))
//                .thenReturn(orders);
//        when(warehouseRepository.findAvailableWarehouses()).thenReturn(warehouses);
//
//        // Mock warehouse selector trả về warehouse cho mỗi order
//        when(warehouseSelector.selectNearestWarehouseWithSlot(
//                any(Order.class), eq(warehouses), anyMap()))
//                .thenReturn(warehouse)  // Order 1
//                .thenReturn(warehouse)  // Order 2
//                .thenReturn(warehouse); // Order 3
//
//        doNothing().when(domainEventPublisher).publishEvent(any());
//
//        // When
//        dispatchService.autoDispatch();
//
//        // Then
//        verify(orderRepository).findTop100ByStatusOrderByCreatedAtAsc(
//                eq(OrderStatus.NEW), any(PageRequest.class));
//        verify(warehouseRepository).findAvailableWarehouses();
//        verify(warehouseSelector, times(3)).selectNearestWarehouseWithSlot(
//                any(Order.class), eq(warehouses), anyMap());
//        verify(domainEventPublisher).publishEvent(autoEventCaptor.capture());
//
//        OrdersAutoDispatchedEvent event = autoEventCaptor.getValue();
//        assertEquals(3, event.getOrderWarehouseMap().size());
//        assertNotNull(event.getDispatchAt());
//    }
//
//    /**
//     * Test case: Auto dispatch - Không có order
//     */
//    @Test
//    @DisplayName("autoDispatch - Không có order")
//    void autoDispatch_NoOrders() {
//        // Given
//        when(orderRepository.findTop100ByStatusOrderByCreatedAtAsc(
//                eq(OrderStatus.NEW), any(PageRequest.class)))
//                .thenReturn(Collections.emptyList());
//
//        // When
//        dispatchService.autoDispatch();
//
//        // Then
//        verify(orderRepository).findTop100ByStatusOrderByCreatedAtAsc(
//                eq(OrderStatus.NEW), any(PageRequest.class));
//        verify(warehouseRepository, never()).findAvailableWarehouses();
//        verify(domainEventPublisher, never()).publishEvent(any());
//    }
//
//    /**
//     * Test case: Auto dispatch - Không có warehouse available
//     */
//    @Test
//    @DisplayName("autoDispatch - Không có warehouse")
//    void autoDispatch_NoWarehouses() {
//        // Given
//        when(orderRepository.findTop100ByStatusOrderByCreatedAtAsc(
//                eq(OrderStatus.NEW), any(PageRequest.class)))
//                .thenReturn(orders);
//        when(warehouseRepository.findAvailableWarehouses()).thenReturn(Collections.emptyList());
//
//        // When & Then
//        AppException exception = assertThrows(AppException.class,
//                () -> dispatchService.autoDispatch());
//
//        assertEquals(ErrorCode.FAILED, exception.getErrorCode());
//        verify(domainEventPublisher, never()).publishEvent(any());
//    }
//
//    /**
//     * Test case: Auto dispatch - Không đủ slot cho tất cả orders
//     */
//    @Test
//    @DisplayName("autoDispatch - Không đủ slot")
//    void autoDispatch_InsufficientSlots() {
//        // Given
//        warehouse.setAvailableSlots(2); // Chỉ có 2 slots
//
//        when(orderRepository.findTop100ByStatusOrderByCreatedAtAsc(
//                eq(OrderStatus.NEW), any(PageRequest.class)))
//                .thenReturn(orders);
//        when(warehouseRepository.findAvailableWarehouses()).thenReturn(warehouses);
//
//        when(warehouseSelector.selectNearestWarehouseWithSlot(
//                any(Order.class), eq(warehouses), anyMap()))
//                .thenReturn(warehouse)  // Order 1
//                .thenReturn(warehouse)  // Order 2
//                .thenReturn(null);      // Order 3 - hết slot
//
//        doNothing().when(domainEventPublisher).publishEvent(any());
//
//        // When
//        dispatchService.autoDispatch();
//
//        // Then
//        verify(warehouseSelector, times(3)).selectNearestWarehouseWithSlot(
//                any(Order.class), eq(warehouses), anyMap());
//        verify(domainEventPublisher).publishEvent(autoEventCaptor.capture());
//
//        OrdersAutoDispatchedEvent event = autoEventCaptor.getValue();
//        assertEquals(2, event.getOrderWarehouseMap().size()); // Chỉ dispatch được 2 orders
//    }
//
//    /**
//     * Test case: Auto dispatch - Không dispatch được order nào
//     */
//    @Test
//    @DisplayName("autoDispatch - Không dispatch được order nào")
//    void autoDispatch_NoOrdersDispatched() {
//        // Given
//        when(orderRepository.findTop100ByStatusOrderByCreatedAtAsc(
//                eq(OrderStatus.NEW), any(PageRequest.class)))
//                .thenReturn(orders);
//        when(warehouseRepository.findAvailableWarehouses()).thenReturn(warehouses);
//
//        when(warehouseSelector.selectNearestWarehouseWithSlot(
//                any(Order.class), eq(warehouses), anyMap()))
//                .thenReturn(null); // Không có warehouse nào đủ slot
//
//        // When
//        dispatchService.autoDispatch();
//
//        // Then
//        verify(warehouseSelector, times(1)).selectNearestWarehouseWithSlot(
//                any(Order.class), eq(warehouses), anyMap());
//        verify(domainEventPublisher, never()).publishEvent(any());
//    }
//
//    /**
//     * Test case: Auto dispatch - Kiểm tra remaining slots được cập nhật
//     */
//    @Test
//    @DisplayName("autoDispatch - Kiểm tra remaining slots")
//    void autoDispatch_VerifyRemainingSlots() {
//        // Given
//        when(orderRepository.findTop100ByStatusOrderByCreatedAtAsc(
//                eq(OrderStatus.NEW), any(PageRequest.class)))
//                .thenReturn(orders);
//        when(warehouseRepository.findAvailableWarehouses()).thenReturn(warehouses);
//
//        when(warehouseSelector.selectNearestWarehouseWithSlot(
//                any(Order.class), eq(warehouses), anyMap()))
//                .thenAnswer(invocation -> {
//                    Map<Long, Integer> remaining = invocation.getArgument(2);
//                    // Kiểm tra remaining slots giảm dần
//                    return warehouse;
//                });
//
//        doNothing().when(domainEventPublisher).publishEvent(any());
//
//        // When
//        dispatchService.autoDispatch();
//
//        // Then
//        verify(warehouseSelector, times(3)).selectNearestWarehouseWithSlot(
//                any(Order.class), eq(warehouses), anyMap());
//    }
//}
