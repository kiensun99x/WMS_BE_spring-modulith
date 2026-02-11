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

    ///  1 tiếng chạy một lần từ 10h --> 22h
    @Scheduled(cron = "0 0 10-22 * * ?")
    public void runBatch() {

        log.info("[RETURN-ORDER-SCHEDULER][START] Trigger Return Order Batch Job");
        batchService.processReturnOrders();
        log.info("[RETURN-ORDER-SCHEDULER][END] End Return Order Batch Job");
    }
}
