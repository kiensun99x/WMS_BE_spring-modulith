package com.rk.WMS.batch.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DispatchEventListener {

    @EventListener
    public void onDispatch(OrderDispatchedEvent event) {
        log.info(
                "EVENT RECEIVED - orderId={}, warehouseId={}",
                event.getOrderId(),
                event.getWarehouseId()
        );
    }
}

