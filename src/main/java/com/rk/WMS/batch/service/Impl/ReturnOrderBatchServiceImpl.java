package com.rk.WMS.batch.service.Impl;

import com.rk.WMS.common.constants.ActorType;
import com.rk.WMS.common.event.DomainEventPublisher;
import com.rk.WMS.batch.event.OrdersReturnedEvent;
import com.rk.WMS.batch.event.ReturnOrderPayload;
import com.rk.WMS.batch.service.ReturnOrderBatchService;
import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.order.service.OrderQueryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "RETURN-ORDER-BATCH")
@Service
@RequiredArgsConstructor
public class ReturnOrderBatchServiceImpl implements ReturnOrderBatchService {

    private static final int MAX_BATCH_SIZE = 100;
    private static final int MAX_FAILED_COUNT = 3;

    private final OrderQueryService orderQueryService;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    @Transactional
    public void processReturnOrders() {

        log.info("[RETURN_BATCH][START] Start processing return orders");

        List<Order> orders = orderQueryService.findFailedOrdersForReturn(
                OrderStatus.FAILED,
                (long) MAX_FAILED_COUNT,
                PageRequest.of(0, MAX_BATCH_SIZE)
        );

        if (orders.isEmpty()) {
            log.info("[RETURN_BATCH][SKIP] No orders eligible for return");
            return;
        }

        List<ReturnOrderPayload> payloads = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Order order : orders) {
            try {
                payloads.add(
                        ReturnOrderPayload.builder()
                                .orderId(order.getId())
                                .orderCode(order.getCode())
                                .warehouseId(order.getWarehouseId())
                                .supplierName(order.getSupplierName())
                                .supplierEmail(order.getSupplierEmail())
                                .receiverName(order.getReceiverName())
                                .receiverEmail(order.getReceiverEmail())
                                .failedDeliveryCount(order.getFailedDeliveryCount())
                                .actor(ActorType.SYSTEM)
                                .eventTime(now)
                                .build()
                );

                log.info(
                        "[RETURN_BATCH][EVENT_READY] orderId={}",
                        order.getId()
                );

            } catch (Exception ex) {
                log.error(
                        "[RETURN_BATCH][FAILED] Prepare event failed | orderId={}, orderCode={}",
                        order.getId(), order.getCode(), ex
                );
            }
        }

        domainEventPublisher.publishEvent(
                new OrdersReturnedEvent(payloads)
        );

        log.info(
                "[RETURN_BATCH][SUCCESS] Batch completed | publishedEvents={}",
                payloads.size()
        );
    }
}
