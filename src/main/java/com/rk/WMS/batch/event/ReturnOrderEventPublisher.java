package com.rk.WMS.batch.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnOrderEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(ReturnOrderEvent event) {
        publisher.publishEvent(event);
    }
}

