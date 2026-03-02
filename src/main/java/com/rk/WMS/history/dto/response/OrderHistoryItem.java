package com.rk.WMS.history.dto.response;

import com.rk.WMS.common.constants.ActorType;
import com.rk.WMS.common.constants.OrderStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderHistoryItem {
  private Long userId;
  private Long failureReasonId;
  private ActorType actorType;
  private LocalDateTime occurredAt;
  private OrderStatus fromStatus;
  private OrderStatus toStatus;
}
