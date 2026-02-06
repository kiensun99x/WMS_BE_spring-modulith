package com.rk.WMS.order.mapper;

import com.rk.WMS.order.dto.response.OrderResponseDTO;
import com.rk.WMS.order.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
  @Mapping(target = "status", expression = "java(order.getStatus().name())")
  @Mapping(target = "statusCode", expression = "java(order.getStatus().getCode())")
  @Mapping(target = "warehouseCode", ignore = true)
  @Mapping(target = "warehouseName", ignore = true)
  OrderResponseDTO toResponseDto(Order order);
}
