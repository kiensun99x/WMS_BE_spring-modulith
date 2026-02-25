package com.rk.WMS.order.event;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ListOrderStatusChangedEvent {
  private List<OrderStatusChangedEvent> orderStatusChangedEventList = new ArrayList<>();

  public ListOrderStatusChangedEvent() {

  }

  public void add(OrderStatusChangedEvent event) {
    orderStatusChangedEventList.add(event);
  }
}
