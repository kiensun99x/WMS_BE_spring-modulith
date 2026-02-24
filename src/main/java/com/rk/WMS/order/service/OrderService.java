package com.rk.WMS.order.service;

import com.rk.WMS.order.dto.request.CreateOrderRequest;
import com.rk.WMS.order.dto.request.SearchOrderRequest;
import com.rk.WMS.order.dto.response.OrderResponse;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

  Page<OrderResponse> getOrders(SearchOrderRequest request, Pageable pageable);

  OrderResponse createOrder(CreateOrderRequest order);

  OrderResponse getOrderById(Long id);

  void handleDispatch(Map<Long, Long> orderWarehouseMap, LocalDateTime dispatchAt);
}

