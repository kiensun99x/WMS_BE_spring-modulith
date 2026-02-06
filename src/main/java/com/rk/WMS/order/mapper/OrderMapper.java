package com.rk.WMS.order.mapper;

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
}
