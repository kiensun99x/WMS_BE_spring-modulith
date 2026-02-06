package com.rk.WMS.order.mapper;

import com.rk.WMS.order.dto.response.OrderResponseDTO;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.warehouse.dto.WarehouseBriefDTO;
import java.util.Map;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
  @Mapping(target = "status", expression = "java(order.getStatus().name())")
  @Mapping(target = "statusCode", expression = "java(order.getStatus().getCode())")
  @Mapping(target = "warehouseName", expression = "java(warehouseMap.get(order.getWarehouseId()).getName())")
  @Mapping(target = "warehouseCode", expression = "java(warehouseMap.get(order.getWarehouseId()).getCode())")
  OrderResponseDTO toResponseDto(Order order, Map<Integer, WarehouseBriefDTO> warehouseMap);
}
