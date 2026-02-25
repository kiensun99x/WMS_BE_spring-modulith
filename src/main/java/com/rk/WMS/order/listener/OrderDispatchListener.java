package com.rk.WMS.order.listener;

import com.rk.WMS.batch.event.OrdersAutoDispatchedEvent;
import com.rk.WMS.batch.event.OrdersManuallyDispatchedEvent;
import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.order.repository.OrderRepository;
import com.rk.WMS.order.service.OrderService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j(topic = "ORDER-SERVICE-LISTENER")
@Component
@RequiredArgsConstructor
public class OrderDispatchListener {

  private final OrderService orderService;

  /**
   * lắng nghe sự kiện AutoDispatch
   *
   * Luồng thực thi:
   * 1) nhận sự kiện
   * 2) lấy ra danh sách đơn hàng được phân phối
   * 3) set trạng thái, kho cho đơn hàng
   * @param event: Map<orderId, warehouseId>, dispatchAt
   */
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onAutoDispatched(OrdersAutoDispatchedEvent event) {
    //Map<orderId, warehouseId>
    Map<Long, Long> mapping = event.getOrderWarehouseMap();
    if (mapping == null || mapping.isEmpty()) {
      return;
    }
    orderService.handleDispatch(mapping, event.getDispatchAt());
    log.info("[AUTO_DISPATCH][ORDERS_UPDATED] count={}", mapping.size());
  }

  /**
   * lắng nghe sự kiện ManualDispatch
   *
   * Luồng thực thi:
   * 1) nhận sự kiện
   * 2) convert data thành dạng map<orderId, warehouseId>
   * 3) lấy ra danh sách đơn hàng được phân phối
   * 4) set trạng thái, kho cho đơn hàng
   * @param event: Map<orderId, warehouseId>, dispatchAt
   */
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
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

    //convert orderIds to map<orderId, warehouseId>
    Map<Long, Long> mapping = new HashMap<>(orderIds.size());
    for (Long orderId : orderIds) {
      if (orderId == null) continue;
      mapping.put(orderId, warehouseId);
    }
    //validate
    if (mapping.isEmpty()) {
      return;
    }

    orderService.handleDispatch(mapping, event.getDispatchAt());
    log.info("[MANUAL_DISPATCH][ORDERS_UPDATED] count={}", mapping.size());
  }
}