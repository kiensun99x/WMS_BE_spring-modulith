package com.rk.WMS.batch.scheduler;

import com.rk.WMS.batch.service.DispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "DISPATCH-SCHEDULER")
public class OrderDispatchJob {

    private final DispatchService dispatchService;

    /// 20 phút chạy 1 lần
    @Scheduled(cron = "0 */20 * * * *")
    public void run() {
        log.info("[DISPATCH-SCHEDULER][START] Start auto dispatch orders");
        dispatchService.autoDispatch();
        log.info("[DISPATCH-SCHEDULER][END] End auto dispatch orders");
    }
}
