package com.rk.WMS.history.service.impl;

import com.rk.WMS.history.model.OrderHistory;
import com.rk.WMS.history.repository.OrderHistoryRepository;
import com.rk.WMS.history.service.OrderHistoryService;
import com.rk.WMS.order.event.OrderStatusChangedEvent;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderHistoryServiceImpl implements OrderHistoryService {
  private final OrderHistoryRepository orderHistoryRepository;

  public OrderHistory createOrderHistory(OrderStatusChangedEvent event) {
    return OrderHistory.builder()
        .orderId(event.getOrderId())
        .userId(event.getUserId())
        .failureReasonId(event.getFailureReasonId())
        .actorType(event.getActorType())
        .createdAt(event.getOccurredAt() != null ? event.getOccurredAt() : LocalDateTime.now())
        .fromStatus(event.getFromStatus())
        .toStatus(event.getToStatus())
        .build();
  }

  @Override
  public OrderHistory save(OrderHistory orderHistory) {
    return orderHistoryRepository.save(orderHistory);
  }

  @Override
  public List<OrderHistory> saveAll(List<OrderHistory> orderHistoryList) {
    return orderHistoryRepository.saveAll(orderHistoryList);
  }
}
