package com.rk.WMS.batch.service;


import com.rk.WMS.batch.event.DomainEventPublisher;
import com.rk.WMS.batch.event.OrdersReturnedEvent;
import com.rk.WMS.batch.event.ReturnOrderPayload;
import com.rk.WMS.batch.service.Impl.ReturnOrderBatchServiceImpl;
import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Mục đích: Kiểm tra business logic xử lý đơn hàng hoàn trả
 */
@ExtendWith(MockitoExtension.class)
class ReturnOrderBatchServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private ReturnOrderBatchServiceImpl returnOrderBatchService;

    @Captor
    private ArgumentCaptor<OrdersReturnedEvent> eventCaptor;

    private List<Order> failedOrders;

    /**
     * Setup trước mỗi test case
     * Khởi tạo dữ liệu mẫu
     */
    @BeforeEach
    void setUp() {
        failedOrders = Arrays.asList(
                createFailedOrder(1L, "ORD-001", 3),
                createFailedOrder(2L, "ORD-002", 3),
                createFailedOrder(3L, "ORD-003", 3)
        );
    }

    /**
     * Tạo đối tượng Order bị FAILED cho test
     */
    private Order createFailedOrder(Long id, String code, int failedCount) {
        Order order = new Order();
        order.setId(id);
        order.setCode(code);
        order.setStatus(OrderStatus.FAILED);
        order.setWarehouseId(100L);
        order.setSupplierName("Supplier " + id);
        order.setSupplierEmail("supplier" + id + "@test.com");
        order.setReceiverName("Receiver " + id);
        order.setReceiverEmail("receiver" + id + "@test.com");
        order.setFailedDeliveryCount(failedCount);
        return order;
    }

    /**
     * Test case: Xử lý đơn hoàn trả thành công
     */
    @Test
    @DisplayName("processReturnOrders - Thành công")
    void processReturnOrders_Success() {
        // Given
        when(orderRepository.findFailedOrdersForReturn(
                eq(OrderStatus.FAILED),
                eq(3L),
                any(PageRequest.class)))
                .thenReturn(failedOrders);

        doNothing().when(domainEventPublisher).publishEvent(any());

        // When
        returnOrderBatchService.processReturnOrders();

        // Then
        verify(orderRepository).findFailedOrdersForReturn(
                eq(OrderStatus.FAILED), eq(3L), any(PageRequest.class));
        verify(domainEventPublisher).publishEvent(eventCaptor.capture());

        OrdersReturnedEvent event = eventCaptor.getValue();
        assertEquals(3, event.getOrders().size());

        // Kiểm tra payload đầu tiên
        ReturnOrderPayload payload = event.getOrders().get(0);
        assertEquals(1L, payload.getOrderId());
        assertEquals("ORD-001", payload.getOrderCode());
        assertEquals(100L, payload.getWarehouseId());
        assertEquals("Supplier 1", payload.getSupplierName());
        assertEquals("supplier1@test.com", payload.getSupplierEmail());
        assertEquals("Receiver 1", payload.getReceiverName());
        assertEquals("receiver1@test.com", payload.getReceiverEmail());
        assertEquals(3, payload.getFailedDeliveryCount());
        assertEquals("system", payload.getActor());
        assertNotNull(payload.getEventTime());
    }

    /**
     * Test case: Không có đơn hàng nào đủ điều kiện hoàn trả
     */
    @Test
    @DisplayName("processReturnOrders - Không có đơn hàng")
    void processReturnOrders_NoOrders() {
        // Given
        when(orderRepository.findFailedOrdersForReturn(
                eq(OrderStatus.FAILED),
                eq(3L),
                any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        // When
        returnOrderBatchService.processReturnOrders();

        // Then
        verify(orderRepository).findFailedOrdersForReturn(
                eq(OrderStatus.FAILED), eq(3L), any(PageRequest.class));
        verify(domainEventPublisher, never()).publishEvent(any());
    }


    /**
     * Test case: Kiểm tra số lượng order trong batch không vượt quá MAX_BATCH_SIZE
     */
    @Test
    @DisplayName("processReturnOrders - Kiểm tra batch size")
    void processReturnOrders_CheckBatchSize() {
        // Given - 150 orders
        List<Order> manyOrders = Collections.nCopies(150, failedOrders.get(0));

        when(orderRepository.findFailedOrdersForReturn(
                eq(OrderStatus.FAILED),
                eq(3L),
                any(PageRequest.class)))
                .thenReturn(manyOrders);

        doNothing().when(domainEventPublisher).publishEvent(any());

        // When
        returnOrderBatchService.processReturnOrders();

        // Then - Vẫn xử lý được nhưng chỉ lấy MAX_BATCH_SIZE = 100 từ repository
        verify(orderRepository).findFailedOrdersForReturn(
                eq(OrderStatus.FAILED), eq(3L), argThat(pageRequest ->
                        pageRequest.getPageSize() == 100));
    }

    /**
     * Test case: Kiểm tra MAX_FAILED_COUNT = 3
     */
    @Test
    @DisplayName("processReturnOrders - Kiểm tra MAX_FAILED_COUNT")
    void processReturnOrders_CheckMaxFailedCount() {
        // Given - Order có failed count = 2 (chưa đủ 3)
        Order orderWithLowFailedCount = createFailedOrder(5L, "ORD-005", 2);

        when(orderRepository.findFailedOrdersForReturn(
                eq(OrderStatus.FAILED),
                eq(3L), // Vẫn query với điều kiện = 3
                any(PageRequest.class)))
                .thenReturn(Arrays.asList(orderWithLowFailedCount));

        // When
        returnOrderBatchService.processReturnOrders();

        // Then - Vẫn xử lý vì query đã filter theo failed count
        verify(domainEventPublisher).publishEvent(any());
    }
}
