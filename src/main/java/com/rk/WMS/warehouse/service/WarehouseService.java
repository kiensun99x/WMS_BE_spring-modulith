package com.rk.WMS.warehouse.service;

import com.rk.WMS.warehouse.dto.WarehouseBriefDTO;
import com.rk.WMS.warehouse.model.Warehouse;
import java.util.Map;
import java.util.Set;

public interface WarehouseService {
  Map<Integer, WarehouseBriefDTO> getByIds(Set<Integer> warehouseIds);
}
