package com.rk.WMS.batch.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OrderDispatchedEvent {

    private Integer orderId;
    private Integer warehouseId;
    private LocalDateTime dispatchedAt;
}

