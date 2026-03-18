package com.rk.WMS.order.controller;

import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.common.response.ApiResponse;
import com.rk.WMS.order.dto.request.ConfirmDeliveryRequest;
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
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j(topic = "ORDER-CONTROLLER")
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

  private final OrderService orderService;

  @GetMapping("/{id}")
  public ApiResponse<OrderResponse> getOrderById(@PathVariable Long id) {
    OrderResponse response = orderService.getOrderById(id);
    log.info("[ORDER][API][REQUEST] Get order by id: {}", id);
    return ApiResponse.<OrderResponse>builder()
        .code(ErrorCode.SUCCESS.getCode())
        .message("Lấy đơn hàng thành công")
        .result(response)
        .build();
  }

  @GetMapping("/")
  public ApiResponse<Page<OrderResponse>> getOrders(
      @RequestParam(required = false) String orderCode,
      @RequestParam(required = false) String supplierPhone,
      @RequestParam(required = false) String receiverPhone,
      @RequestParam(required = false) Integer statusCode,
      @RequestParam(required = false) String warehouseCode,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size).withSort(Sort.by("id").descending());
    SearchOrderRequest request = SearchOrderRequest.builder()
        .orderCode(orderCode)
        .supplierPhone(supplierPhone)
        .receiverPhone(receiverPhone)
        .statusCode(statusCode)
        .warehouseCode(warehouseCode)
        .build();
    Page<OrderResponse> response = orderService.getAllOrders(request, pageable);
    log.info("[ORDER][API][REQUEST] Search orders with request: {}", request.toString());

    return ApiResponse.<Page<OrderResponse>>builder()
        .code(ErrorCode.SUCCESS.getCode())
        .message("Tìm kiếm đơn hàng thành công")
        .result(response)
        .build();
  }

  @GetMapping("/my-warehouse")
  public ApiResponse<Page<OrderResponse>> getMyWarehouseOrders(
      @RequestParam(required = false) String orderCode,
      @RequestParam(required = false) String supplierPhone,
      @RequestParam(required = false) String receiverPhone,
      @RequestParam(required = false) Integer statusCode,
      @RequestParam(required = false) String warehouseCode,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size).withSort(Sort.by("id").descending());
    SearchOrderRequest request = SearchOrderRequest.builder()
        .orderCode(orderCode)
        .supplierPhone(supplierPhone)
        .receiverPhone(receiverPhone)
        .statusCode(statusCode)
        .warehouseCode(warehouseCode)
        .build();

    Page<OrderResponse> response = orderService.getMyWarehouseOrders(request, pageable);
    log.info("[ORDER][API][REQUEST] Search current user's warehouse orders with request: {}", request.toString());

    return ApiResponse.<Page<OrderResponse>>builder()
        .code(ErrorCode.SUCCESS.getCode())
        .message("Tìm kiếm đơn hàng thành công")
        .result(response)
        .build();
  }

  @PostMapping("/")
  public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest order) {
    OrderResponse createdOrder = orderService.createOrder(order);
    log.info("[ORDER][API][REQUEST] Create order with request: {}", order.toString());

    return ApiResponse.<OrderResponse>builder()
        .code(ErrorCode.SUCCESS.getCode())
        .message("Tạo đơn hàng thành công")
        .result(createdOrder)
        .build();
  }

  @PostMapping("/{id}/confirm-delivery")
  public ApiResponse<Void> confirmDelivery(
      @PathVariable Long id,
      @Valid @RequestBody ConfirmDeliveryRequest request
  ) {
    orderService.confirmDelivery(id, request);
    log.info("[ORDER][API][REQUEST] Confirm delivery orderId={}, success={}", id, request.isSuccess());

    return ApiResponse.<Void>builder()
        .code(ErrorCode.SUCCESS.getCode())
        .message("Thành công xác nhận giao hàng")
        .build();
  }
}
