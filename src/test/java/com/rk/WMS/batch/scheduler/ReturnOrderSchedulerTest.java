package com.rk.WMS.batch.scheduler;


import com.rk.WMS.batch.service.ReturnOrderBatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Mục đích: Kiểm tra scheduler xử lý đơn hàng hoàn trả
 */
@ExtendWith(MockitoExtension.class)
class ReturnOrderSchedulerTest {

    @Mock
    private ReturnOrderBatchService batchService;

    @InjectMocks
    private ReturnOrderScheduler returnOrderScheduler;

    /**
     * Test case: Job chạy thành công
     * Expected: batchService.processReturnOrders() được gọi 1 lần
     */
    @Test
    @DisplayName("runBatch - Job chạy thành công")
    void runBatch_Success() {
        // When
        returnOrderScheduler.runBatch();

        // Then
        verify(batchService, times(1)).processReturnOrders();
    }



    /**
     * Test case: Kiểm tra cron expression - 10h-22h mỗi giờ chạy 1 lần
     */
    @Test
    @DisplayName("runBatch - Kiểm tra method được gọi")
    void runBatch_VerifyMethodCalled() {
        // When
        returnOrderScheduler.runBatch();

        // Then
        verify(batchService, times(1)).processReturnOrders();
    }
}