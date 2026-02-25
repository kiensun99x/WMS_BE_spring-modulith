package com.rk.WMS.warehouse.listener;

import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.order.event.OrderStatusChangedEvent;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.order.repository.OrderRepository;
import com.rk.WMS.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseDeliveryListener {

  private final OrderRepository orderRepository;
  private final WarehouseService warehouseService;

  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onOrderDelivered(OrderStatusChangedEvent event) {
    if (event == null || event.getToStatus() != OrderStatus.DELIVERED) {
      return;
    }

    Order order = orderRepository.findById(event.getOrderId()).orElse(null);
    if (order == null) {
      log.warn("[WAREHOUSE][DELIVERED][SKIP] order not found: {}", event.getOrderId());
      return;
    }

    if (event.getWarehouseId() == null) {
      log.warn("[WAREHOUSE][DELIVERED][SKIP] order has no warehouseId: {}", order.getId());
      return;
    }

    warehouseService.releaseSlots(event.getWarehouseId(), 1);
    log.info("[WAREHOUSE][DELIVERED][SLOT_RELEASED] orderId={}, warehouseId={} +1slot", order.getId(), event.getWarehouseId());
  }
}