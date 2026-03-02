package com.rk.WMS.history.listener;

import com.rk.WMS.history.model.OrderHistory;
import com.rk.WMS.history.repository.OrderHistoryRepository;
import com.rk.WMS.history.service.OrderHistoryService;
import com.rk.WMS.order.event.ListOrderStatusChangedEvent;
import com.rk.WMS.order.event.OrderStatusChangedEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusChangeListener {

  private final OrderHistoryService orderHistoryService;

  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onOrderStatusChanged(OrderStatusChangedEvent event) {
    OrderHistory history = orderHistoryService.createOrderHistory(event);
    log.info("[HISTORY][ORDER_STATUS_CHANGED] order_id={}, from={}, to={}, failReason={}", event.getOrderId(), event.getFromStatus(), event.getToStatus(), event.getFailureReasonId());
    orderHistoryService.save(history);
  }

  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onListOrderStatusChanged(ListOrderStatusChangedEvent event) {
    if (event == null || event.getOrderStatusChangedEventList() == null || event.getOrderStatusChangedEventList().isEmpty()) {
      return;
    }

    List<OrderHistory> orderHistoryList = event.getOrderStatusChangedEventList().stream()
        .map(orderHistoryService::createOrderHistory)
        .collect(Collectors.toList());

    log.info("[HISTORY][LIST_ORDER_STATUS_CHANGED] count={}", orderHistoryList.size());
    orderHistoryService.saveAll(orderHistoryList);
  }
}
