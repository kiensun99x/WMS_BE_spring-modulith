package com.rk.WMS.warehouse.listener;

import com.rk.WMS.batch.event.OrdersAutoDispatchedEvent;
import com.rk.WMS.batch.event.OrdersManuallyDispatchedEvent;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.warehouse.model.Warehouse;
import com.rk.WMS.warehouse.repository.WarehouseRepository;
import com.rk.WMS.warehouse.service.WarehouseService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseDispatchListener {

  private final WarehouseRepository warehouseRepository;
  private final WarehouseService warehouseService;

  /**
   * lắng nghe sự kiện AutoDispatch
   *
   * Luồng thực thi:
   * 1) nhận sự kiện
   * 2) lấy ra danh sách đơn hàng được phân phối
   * 3) set số lượng còn trống trong kho
   * @param event: Map<orderId, warehouseId>, dispatchAt
   */
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onAutoDispatched(OrdersAutoDispatchedEvent event) {
    Map<Long, Long> mapping = event.getOrderWarehouseMap();
    if (mapping == null || mapping.isEmpty()) {
      return;
    }
    int result = warehouseService.handleDispatch(mapping, event.getDispatchAt());
    log.info("[AUTO_DISPATCH][WAREHOUSE_SLOTS_UPDATED] {} warehouses affected", result);
  }

  /**
   * lắng nghe sự kiện ManualDispatch
   *
   * Luồng thực thi:
   * 1) nhận sự kiện
   * 2) convert data thành dạng map<orderId, warehouseId>
   * 3) lấy ra danh sách đơn hàng được phân phối
   * 4) set số lượng còn trống trong kho
   * @param event: orderIds, warehouseId, dispatchAt
   */
  @EventListener
  @Transactional
  public void onManuallyDispatched(OrdersManuallyDispatchedEvent event) {
    List<Long> orderIds = event.getOrderIds();
    Long warehouseId = event.getWarehouseId();

    //validate
    if (orderIds == null || orderIds.isEmpty()) {
      return;
    }
    if (warehouseId == null) {
      throw new AppException(ErrorCode.WAREHOUSE_NOT_FOUND);
    }

    // Convert List<orderId> + warehouseId -> Map<orderId, warehouseId>
    Map<Long, Long> mapping = new HashMap<>(orderIds.size());
    for (Long orderId : orderIds) {
      if (orderId == null) continue;
      mapping.put(orderId, warehouseId);
    }

    if (mapping.isEmpty()) {
      return;
    }

    warehouseService.handleDispatch(mapping, event.getDispatchAt());
    log.info("[MANUAL_DISPATCH][WAREHOUSE_SLOTS_UPDATED] warehouseId={} decrement={}", warehouseId, orderIds.size());
  }
}