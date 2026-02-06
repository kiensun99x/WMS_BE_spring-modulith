package com.rk.WMS.batch.service.Impl;

import com.rk.WMS.batch.event.OrderDispatchPublisher;
import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.order.repository.OrderRepository;
import com.rk.WMS.warehouse.model.Warehouse;
import com.rk.WMS.warehouse.repository.WarehouseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.rk.WMS.batch.service.DispatchService;


@Slf4j(topic = "DISPATCH-SERVICE")
@Service
@RequiredArgsConstructor
@Transactional
public class DispatchServiceImpl implements DispatchService {

    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseSelector warehouseSelector;
    private final OrderDispatchPublisher eventPublisher;

    @Override
    public void manualDispatch(List<Integer> orderIds, Integer warehouseId) {

        log.info("[MANUAL_DISPATCH] Request | orderIds={}, warehouseId={}",
                orderIds, warehouseId);

        List<Order> orders = orderRepository.findAllById(orderIds);

        if (orders.isEmpty()) {
            log.warn("[MANUAL_DISPATCH][FAILED] Orders not found | orderIds={}", orderIds);
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();

        for (Order order : orders) {

            if (order.getStatus() != OrderStatus.NEW) {
                log.warn(
                        "[MANUAL_DISPATCH][FAILED] Order not NEW | orderId={}, status={}",
                        order.getId(), order.getStatus()
                );
                throw new AppException(ErrorCode.ORDER_NOT_CONFIRMED);
            }

            order.setWarehouseId(warehouseId);
            order.setStatus(OrderStatus.STORED);
            order.setStoredAt(now);

            // Publish event (warehouse module will handle slot deduction)
            eventPublisher.publish(order);
        }

        orderRepository.saveAll(orders);

        log.info("[MANUAL_DISPATCH][SUCCESS] Dispatch success | orderCount={}, warehouseId={}",
                orders.size(), warehouseId);
    }

    @Override
    public void autoDispatch() {

        log.info("[AUTO_DISPATCH] Start auto dispatch");

        List<Order> orders = orderRepository
                .findTop100ByStatusOrderByCreatedAtAsc(
                        OrderStatus.NEW,
                        PageRequest.of(0, 10)
                );

        if (orders.isEmpty()) {
            log.info("[AUTO_DISPATCH] No orders to dispatch");
            return;
        }

        List<Warehouse> warehouses = warehouseRepository.findAvailableWarehouses();

        if (warehouses.isEmpty()) {
            log.warn("[AUTO_DISPATCH][FAILED] No available warehouses");
            throw new AppException(ErrorCode.FAILED);
        }

        LocalDateTime now = LocalDateTime.now();

        for (Order order : orders) {

            Warehouse selectedWarehouse =
                    warehouseSelector.selectNearestWarehouse(order, warehouses);

            order.setStatus(OrderStatus.STORED);
            order.setStoredAt(now);
            order.setWarehouseId(selectedWarehouse.getWarehouseId());

            orderRepository.save(order);

            eventPublisher.publish(order);
        }

        log.info("[AUTO_DISPATCH][SUCCESS] Auto dispatch completed | orderCount={}",
                orders.size());
    }
}


//@Service
//@RequiredArgsConstructor
//@Transactional
//@Slf4j
//public class DispatchService {
//
//    private final OrderRepository orderRepository;
//    private final WarehouseRepository warehouseRepository;
//    private final WarehouseSelector warehouseSelector;
//    private final OrderDispatchPublisher eventPublisher;
//
//
//    public void autoDispatch() {
//
//        List<Order> orders = orderRepository
//                .findTop100ByStatusOrderByCreatedAtAsc(
//                        OrderStatus.NEW,
//                        PageRequest.of(0, 10)
//                );
//
//        if (orders.isEmpty()) {
//            return;
//        }
//
//        List<Warehouse> warehouses = warehouseRepository.findAvailableWarehouses();
//
//        if (warehouses.isEmpty()) {
//            throw new AppException(ErrorCode.FAILED);
//        }
//
//        LocalDateTime now = LocalDateTime.now();
//
//        for (Order order : orders) {
//
//            Warehouse selectedWarehouse =
//                    warehouseSelector.selectNearestWarehouse(order, warehouses);
//
//            order.setStatus(OrderStatus.STORED);
//            order.setStoredAt(now);
//            order.setWarehouseId(selectedWarehouse.getWarehouseId());
//
//            orderRepository.save(order);
//
//            // publish event
//            eventPublisher.publish(order);
//        }
//    }
//}

