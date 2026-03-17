package com.rk.WMS.warehouse.controller;

import com.rk.WMS.common.currentUser.CurrentUserProvider;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.common.response.ApiResponse;
import com.rk.WMS.warehouse.dto.WarehouseBrief;
import com.rk.WMS.warehouse.model.Warehouse;
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
  private final CurrentUserProvider currentUserProvider;

  @GetMapping()
  public ResponseEntity<List<WarehouseBrief>> getAllWarehouses() {
    List<WarehouseBrief> warehouses = warehouseService.getAll();
    return ResponseEntity.ok(warehouses);
  }

  @GetMapping("/me")
  public ApiResponse<Warehouse> getUserCurrentWarehouse() {
    try{
      Long warehouseId = currentUserProvider.getWarehouseId();
      Warehouse warehouse = warehouseService.getById(warehouseId);
      return ApiResponse.<Warehouse>builder()
          .message("Lấy thông tin kho hàng của người dùng thành công")
          .result(warehouse)
          .build();
    } catch (RuntimeException e) {
      throw new AppException(ErrorCode.AUTHENTICATION_NOT_FOUND);
    }

  }
}
