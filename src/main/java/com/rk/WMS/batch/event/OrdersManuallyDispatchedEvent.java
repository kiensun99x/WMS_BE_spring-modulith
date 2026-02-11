package com.rk.WMS.batch.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrdersManuallyDispatchedEvent {

    private final List<Long> orderIds;
    private final Long warehouseId;
    private final LocalDateTime dispatchAt;
}

