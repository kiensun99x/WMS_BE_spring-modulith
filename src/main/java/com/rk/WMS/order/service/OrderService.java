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

public interface OrderService {
  Page<OrderResponseDTO> getAllOrders(Pageable pageable);
}

