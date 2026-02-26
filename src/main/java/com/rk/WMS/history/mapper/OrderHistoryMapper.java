package com.rk.WMS.history.mapper;

import com.rk.WMS.history.dto.response.OrderHistoryItem;
import com.rk.WMS.history.dto.response.OrderHistoryResponse;
import com.rk.WMS.history.model.OrderHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderHistoryMapper {
  @Mapping(target = "occurredAt", source = "createdAt")
  OrderHistoryItem toResponseDTO(OrderHistory orderHistory);
}
