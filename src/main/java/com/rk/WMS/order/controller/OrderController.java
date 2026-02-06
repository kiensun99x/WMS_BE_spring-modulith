package com.rk.WMS.order.controller;

import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.common.response.ApiResponse;
import com.rk.WMS.order.dto.request.SearchOrderRequestDTO;
import com.rk.WMS.order.dto.response.OrderResponseDTO;
import com.rk.WMS.order.service.OrderService;
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
  public ApiResponse<Page<OrderResponseDTO>> getAllOrders(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    Page<OrderResponseDTO> response = orderService.getAllOrders(pageable);
    log.info("[ORDER][API][REQUEST] Get all orders");

    return ApiResponse.<Page<OrderResponseDTO>>builder()
        .code(ErrorCode.SUCCESS.getCode())
        .message("Lấy danh sách đơn hàng thành công")
        .result(response)
        .build();
  }

  @PostMapping("/")
  public ApiResponse<Page<OrderResponseDTO>> getSearchOrders(
      @RequestBody SearchOrderRequestDTO request,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    Page<OrderResponseDTO> response = orderService.getSearchOrders(request, pageable);
    log.info("[ORDER][API][REQUEST] Search orders with request: {}", request.toString());

    return ApiResponse.<Page<OrderResponseDTO>>builder()
        .code(ErrorCode.SUCCESS.getCode())
        .message("Tìm kiếm đơn hàng thành công")
        .result(response)
        .build();
  }

  @RequestMapping("/test")
  public String test() {
    return "Hello World";
  }
}
