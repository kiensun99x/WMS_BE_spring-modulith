package com.rk.WMS.order.mapper;

import com.rk.WMS.order.dto.request.CreateOrderRequest;
import com.rk.WMS.order.dto.response.OrderResponse;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.warehouse.dto.WarehouseBrief;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
  @Mapping(target = "status", expression = "java(order.getStatus().name())")
  @Mapping(target = "statusCode", expression = "java(order.getStatus().getCode())")
  @Mapping(target = "warehouseName", expression = "java(warehouseMap.get(order.getWarehouseId()).getName())")
  @Mapping(target = "warehouseCode", expression = "java(warehouseMap.get(order.getWarehouseId()).getCode())")
  OrderResponse toResponseDto(Order order, Map<Integer, WarehouseBrief> warehouseMap);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "code", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  Order toEntity(CreateOrderRequest order);

  @Mapping(target = "status", expression = "java(createdOrder.getStatus().name())")
  @Mapping(target = "statusCode", expression = "java(createdOrder.getStatus().getCode())")
  OrderResponse toResponseDto(Order createdOrder);
}
