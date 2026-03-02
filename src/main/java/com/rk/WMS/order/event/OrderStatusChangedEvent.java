package com.rk.WMS.order.event;

import com.rk.WMS.common.constants.ActorType;
import com.rk.WMS.common.constants.OrderStatus;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class OrderStatusChangedEvent {
  private Long orderId;
  private OrderStatus fromStatus;
  private OrderStatus toStatus;
  private LocalDateTime occurredAt;
  private ActorType actorType;        // "SYSTEM" | "USER"
  private Long userId;             // nullable
  private Long failureReasonId;     // nullable
  private Long warehouseId;          // nullable
}
