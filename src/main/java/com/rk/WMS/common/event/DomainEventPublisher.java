package com.rk.WMS.common.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    /**
     * Đăng ký publish event sau khi transaction commit.
     *
     * Event KHÔNG được publish ngay tại thời điểm gọi method này.
     * Event được lưu tạm trong TransactionSynchronizationManager
     * và chỉ được publish khi transaction commit thành công.
     *
     * @param event domain event cần publish
     */
    public void publishEvent(Object event) {

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        publisher.publishEvent(event);
                    }
                }
        );
    }
}

