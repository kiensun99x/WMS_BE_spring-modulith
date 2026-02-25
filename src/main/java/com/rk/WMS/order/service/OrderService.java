package com.rk.WMS.order.service;

import com.rk.WMS.order.dto.request.CreateOrderRequest;
import com.rk.WMS.order.dto.request.SearchOrderRequest;
import com.rk.WMS.order.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

  Page<OrderResponse> getOrders(SearchOrderRequest request, Pageable pageable);

  OrderResponse createOrder(CreateOrderRequest order);

  OrderResponse getOrderById(Long id);
}

