package com.rk.WMS.warehouse.service;

import com.rk.WMS.warehouse.dto.WarehouseBrief;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface WarehouseService {
  List<WarehouseBrief> getAll();
  Map<Integer, WarehouseBrief> getByIds(Set<Integer> warehouseIds);

}
