package com.rk.WMS.batch.event;

import com.rk.WMS.order.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDispatchPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(Order order) {
        publisher.publishEvent(
                new OrderDispatchedEvent(
                        order.getId(),
                        order.getWarehouseId(),
                        order.getStoredAt()
                )
        );
    }
}

