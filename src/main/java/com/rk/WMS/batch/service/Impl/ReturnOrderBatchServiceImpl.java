package com.rk.WMS.batch.service.Impl;

import com.rk.WMS.batch.event.ReturnOrderEvent;
import com.rk.WMS.batch.event.ReturnOrderEventPublisher;
import com.rk.WMS.batch.service.ReturnOrderBatchService;
import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j(topic = "RETURN-ORDER-BATCH")
@Service
@RequiredArgsConstructor
public class ReturnOrderBatchServiceImpl implements ReturnOrderBatchService {

    private static final int MAX_BATCH_SIZE = 100;
    private static final int MAX_FAILED_COUNT = 3;

    private final OrderRepository orderRepository;
    private final ReturnOrderEventPublisher eventPublisher;

    @Override
    @Transactional
    public void processReturnOrders() {

        log.info("[RETURN_BATCH][START] Start processing return orders");

        List<Order> orders = orderRepository.findFailedOrdersForReturn(
                OrderStatus.FAILED,
                MAX_FAILED_COUNT,
                PageRequest.of(0, MAX_BATCH_SIZE)
        );

        if (orders.isEmpty()) {
            log.info("[RETURN_BATCH][SKIP] No orders eligible for return");
            return;
        }

        log.info("[RETURN_BATCH][FOUND] Found {} orders for return", orders.size());

        for (Order order : orders) {
            try {
                processSingleOrder(order);
            } catch (Exception ex) {
                log.error(
                        "[RETURN_BATCH][FAILED] Process order failed | orderId={}, orderCode={}",
                        order.getId(), order.getCode(), ex
                );
            }
        }

        log.info("[RETURN_BATCH][SUCCESS] Batch completed | totalOrders={}", orders.size());
    }

    private void processSingleOrder(Order order) {

        log.info(
                "[RETURN_BATCH][PROCESS] Processing order | orderId={}, orderCode={}",
                order.getId(), order.getCode()
        );

        // Update order
        order.setStatus(OrderStatus.RETURNED);
        order.setReturnedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info(
                "[RETURN_BATCH][UPDATED] Order marked as RETURNED | orderId={}, orderCode={}",
                order.getId(), order.getCode()
        );

        // Publish event
        ReturnOrderEvent event = ReturnOrderEvent.builder()
                .orderCode(order.getCode())
                .warehouseId(order.getWarehouseId())
                .supplierName(order.getSupplierName())
                .supplierEmail(order.getSupplierEmail())
                .receiverName(order.getReceiverName())
                .receiverEmail(order.getReceiverEmail())
                .failedDeliveryCount(order.getFailedDeliveryCount())
                .actor("system")
                .eventTime(LocalDateTime.now())
                .build();

        eventPublisher.publish(event);

        log.info(
                "[RETURN_BATCH][EVENT_PUBLISHED] ReturnOrderEvent published | orderCode={}",
                order.getCode()
        );
    }
}
