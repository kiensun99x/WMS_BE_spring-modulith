package com.rk.WMS.warehouse.service.impl;

import com.rk.WMS.warehouse.dto.WarehouseBriefDTO;
import com.rk.WMS.warehouse.mapper.WarehouseMapper;
import com.rk.WMS.warehouse.model.Warehouse;
import com.rk.WMS.warehouse.repository.WarehouseRepository;
import com.rk.WMS.warehouse.service.WarehouseService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WarehouseServiceImpl implements WarehouseService {
  private final WarehouseRepository warehouseRepository;
  private final WarehouseMapper warehouseMapper;

  @Override
  public List<WarehouseBriefDTO> getAll() {
    return warehouseRepository.findAll().stream()
        .map(warehouseMapper::toWarehouseBriefDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Map<Integer, WarehouseBriefDTO> getByIds(Set<Integer> warehouseIds) {
    if (warehouseIds == null || warehouseIds.isEmpty()) {
      return Collections.emptyMap();
    }

    List<Warehouse> warehouses =
        warehouseRepository.findAllById(warehouseIds);

    return warehouses.stream()
        .map(warehouseMapper::toWarehouseBriefDTO)
        .collect(Collectors.toMap(
            WarehouseBriefDTO::getId,
            Function.identity()
        ));
  }
}
