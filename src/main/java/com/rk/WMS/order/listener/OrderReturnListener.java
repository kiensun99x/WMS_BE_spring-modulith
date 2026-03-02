package com.rk.WMS.order.listener;

import com.rk.WMS.batch.event.OrdersReturnedEvent;
import com.rk.WMS.batch.event.ReturnOrderPayload;
import com.rk.WMS.common.constants.ActorType;
import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.common.event.DomainEventPublisher;
import com.rk.WMS.order.event.ListOrderStatusChangedEvent;
import com.rk.WMS.order.event.OrderStatusChangedEvent;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.order.repository.OrderRepository;
import com.rk.WMS.order.service.OrderService;
import com.rk.WMS.warehouse.service.WarehouseService;
import java.time.LocalDateTime;
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
public class OrderReturnListener {

  private final OrderService orderService;

  /**
   * lắng nghe event và xử lý đơn hàng hoàn trả
   *
   * luồng thực thi:
   * 1) Map payload theo orderId để tra nhanh <orderId, ReturnOrderPayload>
   * 2) Lấy ra list order returned
   * 3) Lặp từng đơn hàng returned:
   *  - Cập nhật trạng thái đơn
   *  - Bulk event
   * 4) Publish event
   *
   * @param event
   */
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onOrdersReturned(OrdersReturnedEvent event) {
    if (event == null || event.getOrders() == null || event.getOrders().isEmpty()) {
      return;
    }
    List<ReturnOrderPayload> payloads = event.getOrders();
    orderService.handleReturn(payloads);
  }
}
