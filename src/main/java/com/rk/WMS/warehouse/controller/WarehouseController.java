package com.rk.WMS.warehouse.controller;

import com.rk.WMS.warehouse.dto.WarehouseBriefDTO;
import com.rk.WMS.warehouse.service.WarehouseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/warehouses")
public class WarehouseController {

  private final WarehouseService warehouseService;

  @GetMapping()
  public ResponseEntity<List<WarehouseBriefDTO>> getAllWarehouses() {
    List<WarehouseBriefDTO> warehouses = warehouseService.getAll();
    return ResponseEntity.ok(warehouses);
  }
}
