//package com.rk.WMS.batch;
//
//import com.rk.WMS.batch.event.OrderDispatchPublisher;
//import com.rk.WMS.batch.service.Impl.DispatchServiceImpl;
//import com.rk.WMS.batch.service.Impl.WarehouseSelector;
//import com.rk.WMS.order.repository.OrderRepository;
//import com.rk.WMS.warehouse.repository.WarehouseRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import com.rk.WMS.common.constants.OrderStatus;
//import com.rk.WMS.common.exception.AppException;
//import com.rk.WMS.order.model.Order;
//import com.rk.WMS.warehouse.model.Warehouse;
//import org.junit.jupiter.api.Test;
//import org.springframework.data.domain.PageRequest;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("Dispatch Service - Unit Tests")
//class DispatchServiceImplTest {
//
//    @Mock
//    OrderRepository orderRepository;
//
//    @Mock
//    WarehouseRepository warehouseRepository;
//
//    @Mock
//    WarehouseSelector warehouseSelector;
//
//    @Mock
//    OrderDispatchPublisher eventPublisher;
//
//    @InjectMocks
//    DispatchServiceImpl dispatchService;
//
//    @Test
//    @DisplayName("Auto dispatch: phân phối đơn NEW về kho gần nhất và cập nhật trạng thái thành công")
//    void autoDispatch_success() {
//
//        Order order = new Order();
//        order.setId(1);
//        order.setStatus(OrderStatus.NEW);
//        order.setReceiverLat(BigDecimal.valueOf(21.01));
//        order.setReceiverLon(BigDecimal.valueOf(105.8));
//
//        Warehouse warehouse = Warehouse.builder()
//                .warehouseId(10)
//                .latitude(BigDecimal.valueOf(21.0))
//                .longitude(BigDecimal.valueOf(105.7))
//                .availableSlots(5)
//                .status(1)
//                .build();
//
//        when(orderRepository.findTop100ByStatusOrderByCreatedAtAsc(
//                eq(OrderStatus.NEW),
//                any(PageRequest.class)
//        )).thenReturn(List.of(order));
//
//        when(warehouseRepository.findAvailableWarehouses())
//                .thenReturn(List.of(warehouse));
//
//        when(warehouseSelector.selectNearestWarehouse(order, List.of(warehouse)))
//                .thenReturn(warehouse);
//
//        dispatchService.autoDispatch();
//
//        assertEquals(OrderStatus.STORED, order.getStatus());
//        assertEquals(warehouse.getWarehouseId(), order.getWarehouseId());
//        assertNotNull(order.getStoredAt());
//
//        verify(orderRepository).save(order);
//        verify(eventPublisher).publish(order);
//    }
//
//    @Test
//    @DisplayName("Auto dispatch: không có đơn NEW thì không xử lý")
//    void autoDispatch_noOrders() {
//
//        when(orderRepository.findTop100ByStatusOrderByCreatedAtAsc(
//                eq(OrderStatus.NEW),
//                any(PageRequest.class)
//        )).thenReturn(List.of());
//
//        dispatchService.autoDispatch();
//
//        verify(orderRepository, never()).save(any());
//        verify(eventPublisher, never()).publish(any());
//    }
//
//    @Test
//    @DisplayName("Auto dispatch: không có kho trống thì ném exception")
//    void autoDispatch_noWarehouse_throwException() {
//
//        Order order = new Order();
//        order.setStatus(OrderStatus.NEW);
//
//        when(orderRepository.findTop100ByStatusOrderByCreatedAtAsc(
//                eq(OrderStatus.NEW),
//                any(PageRequest.class)
//        )).thenReturn(List.of(order));
//
//        when(warehouseRepository.findAvailableWarehouses())
//                .thenReturn(List.of());
//
//        assertThrows(AppException.class,
//                () -> dispatchService.autoDispatch());
//    }
//
//    @Test
//    @DisplayName("Manual dispatch: user chủ động điều phối đơn NEW vào kho chỉ định")
//    void manualDispatch_success() {
//
//        Order order = new Order();
//        order.setId(1);
//        order.setStatus(OrderStatus.NEW);
//
//        when(orderRepository.findAllById(List.of(1)))
//                .thenReturn(List.of(order));
//
//        dispatchService.manualDispatch(List.of(1), 100);
//
//        assertEquals(OrderStatus.STORED, order.getStatus());
//        assertEquals(100, order.getWarehouseId());
//        assertNotNull(order.getStoredAt());
//
//        verify(orderRepository).saveAll(any());
//        verify(eventPublisher).publish(order);
//    }
//
//    @Test
//    @DisplayName("Manual dispatch: đơn không ở trạng thái NEW thì không cho điều phối")
//    void manualDispatch_orderNotNew_throwException() {
//
//        Order order = new Order();
//        order.setId(1);
//        order.setStatus(OrderStatus.STORED);
//
//        when(orderRepository.findAllById(List.of(1)))
//                .thenReturn(List.of(order));
//
//        assertThrows(AppException.class,
//                () -> dispatchService.manualDispatch(List.of(1), 100));
//    }
//}