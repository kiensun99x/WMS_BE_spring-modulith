package com.rk.WMS.report;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rk.WMS.common.constants.ReportType;
import com.rk.WMS.report.controller.ReportController;
import com.rk.WMS.report.dto.request.DeliveryPerformanceReportRequest;
import com.rk.WMS.report.dto.request.WarehouseOrderStatisticReportRequest;
import com.rk.WMS.report.dto.response.ReportFileResponse;
import com.rk.WMS.report.service.DeliveryPerformanceReportService;
import com.rk.WMS.report.service.WarehouseOrderStatisticReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Mục đích: Kiểm tra các API endpoint xuất báo cáo
 */
@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WarehouseOrderStatisticReportService warehouseOrderStatisticReportService;

    @Mock
    private DeliveryPerformanceReportService deliveryPerformanceReportService;

    @InjectMocks
    private ReportController reportController;

    private ObjectMapper objectMapper;

    /**
     * Setup trước mỗi test case
     * Khởi tạo MockMvc với controller
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
    }

    // ==================== WAREHOUSE ORDER STATISTIC TESTS ====================

    /**
     * Test case: Export báo cáo thống kê đơn theo kho thành công
     * Expected:
     * - HTTP Status 200 (OK)
     * - Content-Type: application/octet-stream
     * - Content-Disposition: attachment với filename
     * - Body chứa file content
     */
    @Test
    @DisplayName("POST /reports/warehouse-order-statistic - Thành công")
    void exportWarehouseOrderStatistic_Success() throws Exception {
        // Given
        WarehouseOrderStatisticReportRequest request = WarehouseOrderStatisticReportRequest.builder()
                .type(ReportType.DAY)
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now())
                .warehouseIds(Arrays.asList(1L, 2L, 3L))
                .build();

        byte[] fileContent = "Sample Excel Content".getBytes();
        ReportFileResponse response = ReportFileResponse.builder()
                .fileName("Report01_20231201120000.xlsx")
                .fileContent(fileContent)
                .build();

        when(warehouseOrderStatisticReportService.exportWarehouseOrderStatisticReport(any()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/reports/warehouse-order-statistic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"Report01_20231201120000.xlsx\""))
                .andExpect(content().bytes(fileContent));

        verify(warehouseOrderStatisticReportService, times(1))
                .exportWarehouseOrderStatisticReport(any(WarehouseOrderStatisticReportRequest.class));
    }

    /**
     * Test case: Export báo cáo thống kê đơn theo kho - Request không hợp lệ
     * Expected: Service vẫn được gọi và xử lý validation ở service layer
     */
    @Test
    @DisplayName("POST /reports/warehouse-order-statistic - Request không hợp lệ")
    void exportWarehouseOrderStatistic_InvalidRequest() throws Exception {
        // Given - Request thiếu thông tin
        WarehouseOrderStatisticReportRequest request = WarehouseOrderStatisticReportRequest.builder()
                .type(ReportType.DAY)
                .build();

        byte[] fileContent = "Sample".getBytes();
        ReportFileResponse response = ReportFileResponse.builder()
                .fileName("Report01.xlsx")
                .fileContent(fileContent)
                .build();

        when(warehouseOrderStatisticReportService.exportWarehouseOrderStatisticReport(any()))
                .thenReturn(response);

        // When & Then - Controller vẫn nhận request và forward xuống service
        mockMvc.perform(post("/reports/warehouse-order-statistic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(warehouseOrderStatisticReportService, times(1))
                .exportWarehouseOrderStatisticReport(any());
    }

    // ==================== DELIVERY PERFORMANCE TESTS ====================

    /**
     * Test case: Export báo cáo hiệu suất giao hàng thành công
     * Expected:
     * - HTTP Status 200 (OK)
     * - Content-Type: application/octet-stream
     * - Content-Disposition: attachment với filename
     * - Body chứa file content
     */
    @Test
    @DisplayName("POST /reports/delivery-performance - Thành công")
    void downloadDeliveryPerformanceReport_Success() throws Exception {
        // Given
        DeliveryPerformanceReportRequest request = new DeliveryPerformanceReportRequest();
        request.setType(ReportType.MONTH);
        request.setStartMonth(YearMonth.now().minusMonths(6));
        request.setEndMonth(YearMonth.now());
        request.setWarehouseIds(Arrays.asList(1L, 2L, 3L));

        byte[] fileContent = "Sample Delivery Report".getBytes();
        ReportFileResponse response = ReportFileResponse.builder()
                .fileName("Report02_20231201120000.xlsx")
                .fileContent(fileContent)
                .build();

        when(deliveryPerformanceReportService.exportDeliveryPerformanceReport(any()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/reports/delivery-performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"Report02_20231201120000.xlsx\""))
                .andExpect(content().bytes(fileContent));

        verify(deliveryPerformanceReportService, times(1))
                .exportDeliveryPerformanceReport(any(DeliveryPerformanceReportRequest.class));
    }

    /**
     * Test case: Export báo cáo hiệu suất giao hàng - Request theo ngày
     * Expected: Thành công với tham số ngày
     */
    @Test
    @DisplayName("POST /reports/delivery-performance - Request theo ngày")
    void downloadDeliveryPerformanceReport_DayType() throws Exception {
        // Given
        DeliveryPerformanceReportRequest request = new DeliveryPerformanceReportRequest();
        request.setType(ReportType.DAY);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        request.setWarehouseIds(Arrays.asList(1L, 2L, 3L));

        byte[] fileContent = "Sample".getBytes();
        ReportFileResponse response = ReportFileResponse.builder()
                .fileName("Report02.xlsx")
                .fileContent(fileContent)
                .build();

        when(deliveryPerformanceReportService.exportDeliveryPerformanceReport(any()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/reports/delivery-performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(deliveryPerformanceReportService, times(1))
                .exportDeliveryPerformanceReport(any());
    }
}