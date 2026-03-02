package com.rk.WMS.warehouse.listener;

import com.rk.WMS.batch.event.OrdersReturnedEvent;
import com.rk.WMS.batch.event.ReturnOrderPayload;
import com.rk.WMS.warehouse.model.Warehouse;
import com.rk.WMS.warehouse.service.WarehouseService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseReturnListener {

  private final WarehouseService warehouseService;

  /**
   * Nhận event hoàn hàng và cập nhật slot kho.
   * - Mỗi đơn hoàn về NCC => availableSlots tăng 1.
   * - Gom nhóm theo warehouseId để update 1 lần.
   */
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onOrdersReturned(OrdersReturnedEvent event) {
    if (event == null || event.getOrders() == null || event.getOrders().isEmpty()) {
      return;
    }

    List<ReturnOrderPayload> payloads = event.getOrders();

    // Đếm số lượng hàng hoàn về theo từng kho: <warehouseId, count>
    Map<Long, Integer> countByWarehouse = new HashMap<>();
    for (ReturnOrderPayload p : payloads) {
      if (p == null || p.getWarehouseId() == null) {
        continue;
      }
      countByWarehouse.merge(p.getWarehouseId(), 1, Integer::sum);
    }

    if (countByWarehouse.isEmpty()) {
      log.warn("[WAREHOUSE][RETURN][SKIP] No warehouseId in returned payloads, payloadCount={}", payloads.size());
      return;
    }

    // Update slot
    List<Warehouse> warehouses = warehouseService.findAllById(countByWarehouse.keySet());
    int totalReturned = 0;
    for (Warehouse warehouse : warehouses) {
      warehouse.setAvailableSlots(warehouse.getAvailableSlots() + countByWarehouse.get(warehouse.getWarehouseId()));
      totalReturned += countByWarehouse.get(warehouse.getWarehouseId());
    }
    warehouseService.update(warehouses);
    log.info(
        "[WAREHOUSE][RETURN][SLOTS_UPDATED] warehousesAffected={}, returnedCount={}",
        countByWarehouse.size(), totalReturned
    );
  }
}
