package com.rk.WMS.order.controller;

import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.common.response.ApiResponse;
import com.rk.WMS.order.dto.request.CreateOrderRequest;
import com.rk.WMS.order.dto.request.SearchOrderRequest;
import com.rk.WMS.order.dto.response.OrderResponse;
import com.rk.WMS.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j(topic = "ORDER-CONTROLLER")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

  private final OrderService orderService;

  @GetMapping("/")
  public ApiResponse<Page<OrderResponse>> getAllOrders(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    Page<OrderResponse> response = orderService.getAllOrders(pageable);
    log.info("[ORDER][API][REQUEST] Get all orders");

    return ApiResponse.<Page<OrderResponse>>builder()
        .code(ErrorCode.SUCCESS.getCode())
        .message("Lấy danh sách đơn hàng thành công")
        .result(response)
        .build();
  }

  @PostMapping("/")
  public ApiResponse<Page<OrderResponse>> getSearchOrders(
      @RequestBody SearchOrderRequest request,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    Page<OrderResponse> response = orderService.getSearchOrders(request, pageable);
    log.info("[ORDER][API][REQUEST] Search orders with request: {}", request.toString());

    return ApiResponse.<Page<OrderResponse>>builder()
        .code(ErrorCode.SUCCESS.getCode())
        .message("Tìm kiếm đơn hàng thành công")
        .result(response)
        .build();
  }

  @PostMapping("/create")
  public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest order) {
    OrderResponse createdOrder = orderService.createOrder(order);
    log.info("[ORDER][API][REQUEST] Create order with request: {}", order.toString());

    return ApiResponse.<OrderResponse>builder()
        .code(ErrorCode.SUCCESS.getCode())
        .message("Tạo đơn hàng thành công")
        .result(createdOrder)
        .build();
  }

  @RequestMapping("/test")
  public String test() {
    return "Hello World";
  }
}
