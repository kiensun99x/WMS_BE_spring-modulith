package com.rk.WMS.history.service.impl;

import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.history.dto.response.OrderHistoryItem;
import com.rk.WMS.history.dto.response.OrderHistoryResponse;
import com.rk.WMS.history.mapper.OrderHistoryMapper;
import com.rk.WMS.history.model.FailureReason;
import com.rk.WMS.history.model.OrderHistory;
import com.rk.WMS.history.repository.FailureReasonRepository;
import com.rk.WMS.history.repository.OrderHistoryRepository;
import com.rk.WMS.history.service.FailureReasonService;
import com.rk.WMS.history.service.OrderHistoryService;
import com.rk.WMS.order.event.OrderStatusChangedEvent;
import com.rk.WMS.order.service.OrderService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderHistoryServiceImpl implements OrderHistoryService {
  private final OrderHistoryRepository orderHistoryRepository;
  private final OrderService orderService;
  private final FailureReasonService failureReasonService;
  private final OrderHistoryMapper orderHistoryMapper;
  private final FailureReasonRepository failureReasonRepository;

  @Override
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

  @Override
  public OrderHistoryResponse getByOrderId(Long orderId) {
    //kiểm tra xem có đơn hàng trong hệ thống không
    orderService.getOrderById(orderId);
    //lấy map failure reason để gán cho nhanh Map<failureReasonId, description>
    List<FailureReason> failureReasons = failureReasonService.getFailureReasons();
    Map<Long, String> failureReasonMap = failureReasons.stream()
        .collect(Collectors
            .toMap(FailureReason::getReasonId, FailureReason::getDescription));
    //map từ List orderHistory -> List OrderHistoryDTO
    List<OrderHistoryItem> response = orderHistoryRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
        .stream()
        .map(orderHistory -> orderHistoryMapper.toResponseDto(orderHistory, failureReasonMap))
        .toList();
    return new OrderHistoryResponse(orderId, response);
  }

  public FailureReason getFailureReasonById(Long id){
    return failureReasonRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.FAILURE_REASON_NOT_FOUND));
  }
}
