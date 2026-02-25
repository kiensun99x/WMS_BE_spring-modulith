package com.rk.WMS.warehouse.service;

import com.rk.WMS.warehouse.dto.WarehouseBrief;
import com.rk.WMS.warehouse.model.Warehouse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface WarehouseService {
  List<WarehouseBrief> getAll();
  Map<Long, WarehouseBrief> getByIds(Set<Long> warehouseIds);

  Warehouse getById(Long warehouseId);
  int handleDispatch(Map<Long, Long> orderWarehouseMap, LocalDateTime dispatchAt);
  int releaseSlots(Long warehouseId, int increment);

  List<Warehouse> findAllById(Set<Long> longs);
  void update(List<Warehouse> warehouses);
}
