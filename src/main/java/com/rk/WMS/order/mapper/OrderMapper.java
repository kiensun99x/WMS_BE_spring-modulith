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
  //dùng để loop map nhiều đơn hàng với thông tin warehouse

  /**
   * Mapping đơn hàng kèm thông tin warehouse tương ứng
   *
   * @param order: Order
   * @param warehouseMap: Map<warehouseId, warehouseBrief>
   * @return OrderDTO chứa thông tin warehouse, status
   */
  @Mapping(target = "status", expression = "java(order.getStatus().name())")
  @Mapping(target = "statusCode", expression = "java(order.getStatus().getCode())")
  @Mapping(target = "warehouseName", expression = "java(getWarehouseName(order.getWarehouseId(), warehouseMap))")
  @Mapping(target = "warehouseCode", expression = "java(getWarehouseCode(order.getWarehouseId(), warehouseMap))")
  OrderResponse toResponseDto(Order order, Map<Integer, WarehouseBrief> warehouseMap);

  //map 1 order chưa chứa thông tin warehouse
  /**
   * Trả về OrderDTO mà người dùng vừa tạo thành công
   *
   * chứa thông tin status, chưa có thông tin warehouse
   * @param createdOrder: Order
   * @return OrderDTO
   */
  @Mapping(target = "status", expression = "java(createdOrder.getStatus().name())")
  @Mapping(target = "statusCode", expression = "java(createdOrder.getStatus().getCode())")
  OrderResponse toResponseDto(Order createdOrder);

  /**
   * Mapping requestDTO thành entity
   *
   * @param order: OrderDTO
   * @return Order không chứa các thông tin như ID, code, status, createdAt
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "code", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  Order toEntity(CreateOrderRequest order);

  /**
   * lấy tên kho từ id
   * @param warehouseId
   * @param warehouseMap: <warehouseID, warehouseBrief>
   * @return tên kho
   */
  default String getWarehouseName(
      Integer warehouseId,
      Map<Integer, WarehouseBrief> warehouseMap
  ) {
    if (warehouseId == null) {
      return null;
    }
    return warehouseMap.get(warehouseId).getName();
  }

  /**
   * lấy mã kho từ id
   * @param warehouseId
   * @param warehouseMap: <warehouseID, warehouseBrief>
   * @return mã kho
   */
  default String getWarehouseCode(
      Integer warehouseId,
      Map<Integer, WarehouseBrief> warehouseMap
  ) {
    if (warehouseId == null) {
      return null;
    }
    return warehouseMap.get(warehouseId).getCode();
  }
}
