package com.rk.WMS.history.mapper;

import com.rk.WMS.history.dto.response.OrderHistoryItem;
import com.rk.WMS.history.model.OrderHistory;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderHistoryMapper {
  @Mapping(target = "occurredAt", source = "orderHistory.createdAt")
  @Mapping(target = "failureReason", expression = "java(failureReasonMap.get(orderHistory.getFailureReasonId()))")
  OrderHistoryItem toResponseDto(OrderHistory orderHistory, Map<Long, String> failureReasonMap);
}
