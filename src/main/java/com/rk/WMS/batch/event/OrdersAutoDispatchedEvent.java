package com.rk.WMS.batch.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
public class OrdersAutoDispatchedEvent {

    /**
     * key   = orderId
     * value = warehouseId
     */
    private final Map<Long, Long> orderWarehouseMap;

    private final LocalDateTime dispatchAt;
}

