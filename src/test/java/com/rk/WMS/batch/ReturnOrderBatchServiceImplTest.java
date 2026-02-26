//package com.rk.WMS.batch;
//
//import com.rk.WMS.batch.event.ReturnOrderEvent;
//import com.rk.WMS.batch.event.ReturnOrderEventPublisher;
//import com.rk.WMS.batch.service.Impl.ReturnOrderBatchServiceImpl;
//import com.rk.WMS.common.constants.OrderStatus;
//import com.rk.WMS.order.model.Order;
//import com.rk.WMS.order.repository.OrderRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.springframework.data.domain.PageRequest;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
//@DisplayName("Unit Test - Batch xử lý hoàn hàng do giao thất bại quá số lần")
//class ReturnOrderBatchServiceImplTest {
//
//    @Mock
//    private OrderRepository orderRepository;
//
//    @Mock
//    private ReturnOrderEventPublisher eventPublisher;
//
//    @InjectMocks
//    private ReturnOrderBatchServiceImpl batchService;
//
//    private Order failedOrder;
//
//    @BeforeEach
//    void setUp() {
//        failedOrder = new Order();
//        failedOrder.setId(1);
//        failedOrder.setCode("ORD-001");
//        failedOrder.setStatus(OrderStatus.FAILED);
//        failedOrder.setWarehouseId(10);
//        failedOrder.setSupplierName("NCC A");
//        failedOrder.setSupplierEmail("ncca@test.com");
//        failedOrder.setReceiverName("BNH B");
//        failedOrder.setReceiverEmail("bnhb@test.com");
//        failedOrder.setFailedDeliveryCount(3);
//        failedOrder.setUpdatedAt(LocalDateTime.now());
//    }
//
//
//    @Test
//    @DisplayName("CASE 1 - Đơn hàng giao thất bại ≥ 3 lần")
//    void should_return_order_and_publish_event() {
//
//        // GIVEN
//        when(orderRepository.findFailedOrdersForReturn(
//                eq(OrderStatus.FAILED),
//                eq(3),
//                any(PageRequest.class)
//        )).thenReturn(List.of(failedOrder));
//
//        when(orderRepository.save(any(Order.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//        // WHEN
//        batchService.processReturnOrders();
//
//        // THEN
//        // 1. Cập nhật trạng thái
//        assertThat(failedOrder.getStatus()).isEqualTo(OrderStatus.RETURNED);
//        assertThat(failedOrder.getReturnedAt()).isNotNull();
//
//        // 2. Lưu DB
//        verify(orderRepository, times(1)).save(failedOrder);
//
//        // 3. Bắn event hoàn hàng 1 lần
//        verify(eventPublisher, times(1))
//                .publish(any(ReturnOrderEvent.class));
//    }
//
//
//    @Test
//    @DisplayName("CASE 2 - Không có đơn hàng thỏa điều kiện → không cập nhật và không bắn event")
//    void should_do_nothing_when_no_orders_found() {
//
//        // GIVEN
//        when(orderRepository.findFailedOrdersForReturn(
//                eq(OrderStatus.FAILED),
//                eq(3),
//                any(PageRequest.class)
//        )).thenReturn(List.of());
//
//        // WHEN
//        batchService.processReturnOrders();
//
//        // THEN
//        verify(orderRepository, never()).save(any());
//        verify(eventPublisher, never()).publish(any());
//    }
//
//
//    @Test
//    @DisplayName("CASE 3 - Event hoàn hàng được bắn ra chứa đúng thông tin đơn hàng")
//    void should_publish_event_with_correct_data() {
//
//        // GIVEN
//        when(orderRepository.findFailedOrdersForReturn(
//                any(),
//                anyInt(),
//                any(PageRequest.class)
//        )).thenReturn(List.of(failedOrder));
//
//        ArgumentCaptor<ReturnOrderEvent> eventCaptor =
//                ArgumentCaptor.forClass(ReturnOrderEvent.class);
//
//        // WHEN
//        batchService.processReturnOrders();
//
//        // THEN
//        verify(eventPublisher).publish(eventCaptor.capture());
//
//        ReturnOrderEvent event = eventCaptor.getValue();
//
//        assertThat(event.getOrderCode()).isEqualTo("ORD-001");
//        assertThat(event.getWarehouseId()).isEqualTo(10);
//        assertThat(event.getSupplierName()).isEqualTo("NCC A");
//        assertThat(event.getReceiverName()).isEqualTo("BNH B");
//        assertThat(event.getFailedDeliveryCount()).isEqualTo(3);
//        assertThat(event.getActor()).isEqualTo("system");
//        assertThat(event.getEventTime()).isNotNull();
//    }
//}
//
