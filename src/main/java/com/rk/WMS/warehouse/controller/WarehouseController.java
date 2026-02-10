package com.rk.WMS.warehouse.controller;

import com.rk.WMS.warehouse.dto.WarehouseBrief;
import com.rk.WMS.warehouse.service.WarehouseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j(topic = "WAREHOUSE-CONTROLLER")
@RequiredArgsConstructor
@RestController
@RequestMapping("/warehouses")
public class WarehouseController {

  private final WarehouseService warehouseService;
  @GetMapping()
  public ResponseEntity<List<WarehouseBrief>> getAllWarehouses() {
    List<WarehouseBrief> warehouses = warehouseService.getAll();
    return ResponseEntity.ok(warehouses);
  }
}
