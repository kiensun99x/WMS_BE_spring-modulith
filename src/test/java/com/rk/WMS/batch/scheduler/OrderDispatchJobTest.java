package com.rk.WMS.batch.scheduler;


import com.rk.WMS.batch.service.DispatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Mục đích: Kiểm tra scheduler tự động dispatch đơn hàng
 */
@ExtendWith(MockitoExtension.class)
class OrderDispatchJobTest {

    @Mock
    private DispatchService dispatchService;

    @InjectMocks
    private OrderDispatchJob orderDispatchJob;

    /**
     * Test case: Job chạy thành công
     * Expected: dispatchService.autoDispatch() được gọi 1 lần
     */
    @Test
    @DisplayName("run - Job chạy thành công")
    void run_Success() {
        // When
        orderDispatchJob.run();

        // Then
        verify(dispatchService, times(1)).autoDispatch();
    }


    /**
     * Test case: Kiểm tra cron expression - 20 phút chạy 1 lần
     * Không thể test trực tiếp, chỉ verify method được gọi
     */
    @Test
    @DisplayName("run - Kiểm tra method được gọi")
    void run_VerifyMethodCalled() {
        // When
        orderDispatchJob.run();

        // Then
        verify(dispatchService, times(1)).autoDispatch();
    }
}
