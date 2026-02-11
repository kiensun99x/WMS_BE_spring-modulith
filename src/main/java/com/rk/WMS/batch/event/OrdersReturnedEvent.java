package com.rk.WMS.batch.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrdersReturnedEvent {

    private final List<ReturnOrderPayload> orders;
}

