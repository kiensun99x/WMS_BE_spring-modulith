package com.rk.WMS.warehouse.service.impl;

import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.warehouse.dto.WarehouseBrief;
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

  /**
   * Lấy danh sách toàn bộ kho chứa các thông tin cơ bản(ID, name, code)
   * @return danh sách kho
   */
  @Override
  public List<WarehouseBrief> getAll() {
    return warehouseRepository.findAll().stream()
        .map(warehouseMapper::toWarehouseBriefDTO)
        .collect(Collectors.toList());
  }

  /**
   * Lấy danh sách kho theo danh sách id
   * @param warehouseIds
   * @return Map<warehouseId, WarehouseBrief>
   */
  @Override
  public Map<Long, WarehouseBrief> getByIds(Set<Long> warehouseIds) {
    if (warehouseIds == null || warehouseIds.isEmpty()) {
      return Collections.emptyMap();
    }

    List<Warehouse> warehouses =
        warehouseRepository.findAllById(warehouseIds);

    return warehouses.stream()
        .map(warehouseMapper::toWarehouseBriefDTO)
        .collect(Collectors.toMap(
            WarehouseBrief::getId,
            Function.identity()
        ));
  }

  /**
   * Lấy kho theo id
   * @param warehouseId
   * @return Warehouse entity với toàn bộ thông tin
   */
  @Override
  public Warehouse getById(Long warehouseId) {
    return warehouseRepository.findById(warehouseId).orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
  }
}
