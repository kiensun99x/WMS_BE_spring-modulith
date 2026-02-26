package com.rk.WMS.history.service;

import com.rk.WMS.history.dto.response.OrderHistoryResponse;
import com.rk.WMS.history.model.OrderHistory;
import com.rk.WMS.order.event.OrderStatusChangedEvent;
import java.util.List;

public interface OrderHistoryService {
  public OrderHistory createOrderHistory(OrderStatusChangedEvent event);

  public OrderHistory save(OrderHistory orderHistory);

  public List<OrderHistory> saveAll(List<OrderHistory> orderHistoryList);
  public OrderHistoryResponse getByOrderId(Long orderId);
}
