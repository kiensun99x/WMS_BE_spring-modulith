package com.rk.WMS.batch.schedular;

import com.rk.WMS.batch.service.DispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderDispatchJob {

    private final DispatchService dispatchService;

//    @Scheduled(cron = "0 */20 * * * *")
    @Scheduled(cron = "*/30 * * * * *") //30s để chạy thử test
    public void run() {
        log.info("Start auto dispatch orders");
        dispatchService.autoDispatch();
        log.info("End auto dispatch orders");
    }
}
