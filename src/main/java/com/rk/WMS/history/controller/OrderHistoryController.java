package com.rk.WMS.history.controller;

import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.common.response.ApiResponse;
import com.rk.WMS.history.dto.response.OrderHistoryResponse;
import com.rk.WMS.history.service.OrderHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j(topic = "ORDER-HISTORY-CONTROLLER")
@RestController
@RequiredArgsConstructor
public class OrderHistoryController {
  private final OrderHistoryService orderHistoryService;

  @GetMapping("orders/{order_id}/histories")
  public ApiResponse<OrderHistoryResponse> getOrderHistories(@PathVariable Long order_id) {
    OrderHistoryResponse response = orderHistoryService.getByOrderId(order_id);
    log.info("Fetched order histories for order {}", order_id);
    return ApiResponse.<OrderHistoryResponse>builder()
        .code(ErrorCode.SUCCESS.getCode())
        .message("Lấy lịch sử đơn hàng thành công")
        .result(response)
        .build();
  }
}
