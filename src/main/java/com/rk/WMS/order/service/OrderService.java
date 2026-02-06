package com.rk.WMS.order.service;

import com.rk.WMS.order.dto.response.OrderResponseDTO;
import com.rk.WMS.order.mapper.OrderMapper;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.order.repository.OrderRepository;
import com.rk.WMS.warehouse.dto.WarehouseBriefDTO;
import com.rk.WMS.warehouse.service.WarehouseService;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderService {
  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;
  private final WarehouseService warehouseService;

  public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {

    Page<Order> orders = orderRepository.findAll(pageable);

    // map entity -> dto
    Page<OrderResponseDTO> dtoPage = orders.map(orderMapper::toResponseDto);

    // lấy danh sách warehouseId
    Set<Integer> warehouseIds = orders.stream()
        .map(Order::getWarehouseId)
        .collect(Collectors.toSet());

    Map<Integer, WarehouseBriefDTO> warehouseMap =
        warehouseService.getByIds(warehouseIds);

    // enrich
    dtoPage.forEach(dto -> {
      WarehouseBriefDTO wh = warehouseMap.get(dto.getWarehouseId());
      if (wh != null) {
        dto.setWarehouseCode(wh.getCode());
        dto.setWarehouseName(wh.getName());
      }
    });

    return dtoPage;
  }

}
