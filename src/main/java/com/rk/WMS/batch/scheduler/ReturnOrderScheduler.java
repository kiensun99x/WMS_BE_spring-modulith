package com.rk.WMS.batch.scheduler;

import com.rk.WMS.batch.service.ReturnOrderBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Slf4j(topic = "RETURN-ORDER-SCHEDULER")
@Component
@RequiredArgsConstructor
public class ReturnOrderScheduler {

    private final ReturnOrderBatchService batchService;

//    @Scheduled(cron = "0 0 * * * ?")
    @Scheduled(cron = "*/30 * * * * ?")
    public void runBatch() {

        LocalTime now = LocalTime.now();

        if (now.isBefore(LocalTime.of(10, 0)) ||
                now.isAfter(LocalTime.of(22, 0))) {
            return;
        }

        log.info("[RETURN-ORDER-SCHEDULER][START] Trigger Return Order Batch Job");
        batchService.processReturnOrders();
        log.info("[RETURN-ORDER-SCHEDULER][START] End Return Order Batch Job");
    }
}
