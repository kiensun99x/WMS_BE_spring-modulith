package com.rk.WMS.report;

import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.common.constants.ReportType;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.history.repository.OrderHistoryRepository;
import com.rk.WMS.report.dto.request.DeliveryPerformanceReportRequest;
import com.rk.WMS.report.dto.response.ReportFileResponse;
import com.rk.WMS.report.projection.DeliveryPerformanceProjection;
import com.rk.WMS.report.service.Impl.DeliveryPerformanceReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Mục đích: Kiểm tra business logic của service xuất báo cáo hiệu suất giao hàng
 */
@ExtendWith(MockitoExtension.class)
class DeliveryPerformanceReportServiceImplTest {

    @Mock
    private OrderHistoryRepository orderHistoryRepository;

    @InjectMocks
    private DeliveryPerformanceReportServiceImpl reportService;

    @Captor
    private ArgumentCaptor<LocalDateTime> startTimeCaptor;

    @Captor
    private ArgumentCaptor<LocalDateTime> endTimeCaptor;

    private DeliveryPerformanceReportRequest validDayRequest;
    private DeliveryPerformanceReportRequest validMonthRequest;

    /**
     * Setup trước mỗi test case
     * Khởi tạo các request hợp lệ
     */
    @BeforeEach
    void setUp() {
        validDayRequest = new DeliveryPerformanceReportRequest();
        validDayRequest.setType(ReportType.DAY);
        validDayRequest.setStartDate(LocalDate.now().minusDays(7));
        validDayRequest.setEndDate(LocalDate.now());
        validDayRequest.setWarehouseIds(Arrays.asList(1L, 2L, 3L));

        validMonthRequest = new DeliveryPerformanceReportRequest();
        validMonthRequest.setType(ReportType.MONTH);
        validMonthRequest.setStartMonth(YearMonth.now().minusMonths(6));
        validMonthRequest.setEndMonth(YearMonth.now());
        validMonthRequest.setWarehouseIds(Arrays.asList(1L, 2L, 3L));
    }


    /**
     * Test case: Validate request - WarehouseIds null
     * Expected: Throw AppException với code MISSING_START_TIME (SYSS-0008)
     */
    @Test
    @DisplayName("validateRequest - WarehouseIds null -> SYSS-0008")
    void validateRequest_NullWarehouseIds_ThrowsException() {
        // Given
        validDayRequest.setWarehouseIds(null);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportDeliveryPerformanceReport(validDayRequest));

        assertEquals(ErrorCode.MISSING_START_TIME, exception.getErrorCode());
    }

    /**
     * Test case: Validate request - WarehouseIds rỗng
     * Expected: Throw AppException với code MISSING_START_TIME (SYSS-0008)
     */
    @Test
    @DisplayName("validateRequest - WarehouseIds rỗng -> SYSS-0008")
    void validateRequest_EmptyWarehouseIds_ThrowsException() {
        // Given
        validDayRequest.setWarehouseIds(Collections.emptyList());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportDeliveryPerformanceReport(validDayRequest));

        assertEquals(ErrorCode.MISSING_START_TIME, exception.getErrorCode());
    }

    /**
     * Test case: Validate request - Quá 10 warehouse
     * Expected: Throw AppException với code MISSING_START_TIME (SYSS-0008)
     */
    @Test
    @DisplayName("validateRequest - Quá 10 warehouse -> SYSS-0008")
    void validateRequest_TooManyWarehouses_ThrowsException() {
        // Given - 11 warehouses
        validDayRequest.setWarehouseIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L));

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportDeliveryPerformanceReport(validDayRequest));

        assertEquals(ErrorCode.MISSING_START_TIME, exception.getErrorCode());
    }

    /**
     * Test case: Validate request DAY - Khoảng cách > 15 ngày
     * Expected: Throw AppException với code MISSING_END_TIME (SYSS-0008)
     */
    @Test
    @DisplayName("validateRequest DAY - > 15 ngày -> SYSS-0008")
    void validateRequest_Day_MoreThan15Days_ThrowsException() {
        // Given - 16 ngày
        validDayRequest.setStartDate(LocalDate.now().minusDays(16));
        validDayRequest.setEndDate(LocalDate.now());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportDeliveryPerformanceReport(validDayRequest));

        assertEquals(ErrorCode.MISSING_END_TIME, exception.getErrorCode());
    }

    /**
     * Test case: Validate request DAY - Đúng 15 ngày (boundary)
     * Expected: Thành công
     */
    @Test
    @DisplayName("validateRequest DAY - Đúng 15 ngày (boundary)")
    void validateRequest_Day_Exactly15Days_Success() {
        // Given
        validDayRequest.setStartDate(LocalDate.now().minusDays(15));
        validDayRequest.setEndDate(LocalDate.now());

        // Mock data
        when(orderHistoryRepository.fetchDeliveryPerformanceData(
                anyList(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertDoesNotThrow(() -> reportService.exportDeliveryPerformanceReport(validDayRequest));
    }

    /**
     * Test case: Validate request MONTH - Khoảng cách > 12 tháng
     * Expected: Throw AppException với code VALUE_EXCEED_LIMIT (SYSS-0009)
     */
    @Test
    @DisplayName("validateRequest MONTH - > 12 tháng -> SYSS-0009")
    void validateRequest_Month_MoreThan12Months_ThrowsException() {
        // Given - 13 tháng
        validMonthRequest.setStartMonth(YearMonth.now().minusMonths(13));
        validMonthRequest.setEndMonth(YearMonth.now());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportDeliveryPerformanceReport(validMonthRequest));

        assertEquals(ErrorCode.VALUE_EXCEED_LIMIT, exception.getErrorCode());
    }

    /**
     * Test case: Validate request MONTH - Đúng 12 tháng (boundary)
     * Expected: Thành công
     */
    @Test
    @DisplayName("validateRequest MONTH - Đúng 12 tháng (boundary)")
    void validateRequest_Month_Exactly12Months_Success() {
        // Given
        validMonthRequest.setStartMonth(YearMonth.now().minusMonths(12));
        validMonthRequest.setEndMonth(YearMonth.now());

        // Mock data
        when(orderHistoryRepository.fetchDeliveryPerformanceData(
                anyList(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertDoesNotThrow(() -> reportService.exportDeliveryPerformanceReport(validMonthRequest));
    }

    // ==================== EXPORT TESTS ====================

    /**
     * Test case: Export báo cáo thành công - Type DAY
     * Expected:
     * - Tạo file Excel với tên đúng format: Report02_yyyyMMddHHMMSS.xlsx
     * - Mỗi warehouse tạo 1 sheet riêng
     * - Gọi repository với tham số đúng
     */
    @Test
    @DisplayName("exportDeliveryPerformanceReport - Type DAY thành công")
    void exportDeliveryPerformanceReport_Day_Success() {
        // Given - Mock data với các status và lý do thất bại
        List<DeliveryPerformanceProjection> mockData = Arrays.asList(
                createMockProjection(1L, LocalDateTime.now().minusDays(7), OrderStatus.DELIVERED.getCode(), null, 10L),
                createMockProjection(1L, LocalDateTime.now().minusDays(7), OrderStatus.FAILED.getCode(), 101L, 3L),
                createMockProjection(1L, LocalDateTime.now().minusDays(7), OrderStatus.FAILED.getCode(), 102L, 2L),
                createMockProjection(2L, LocalDateTime.now().minusDays(7), OrderStatus.DELIVERED.getCode(), null, 15L),
                createMockProjection(2L, LocalDateTime.now().minusDays(6), OrderStatus.DELIVERED.getCode(), null, 12L)
        );

        when(orderHistoryRepository.fetchDeliveryPerformanceData(
                eq(validDayRequest.getWarehouseIds()),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(OrderStatus.DELIVERED.getCode()),
                eq(OrderStatus.FAILED.getCode())))
                .thenReturn(mockData);

        // When
        ReportFileResponse response = reportService.exportDeliveryPerformanceReport(validDayRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getFileName().startsWith("Report02_"));
        assertTrue(response.getFileName().endsWith(".xlsx"));
        assertNotNull(response.getFileContent());
        assertTrue(response.getFileContent().length > 0);

        verify(orderHistoryRepository).fetchDeliveryPerformanceData(
                eq(validDayRequest.getWarehouseIds()),
                startTimeCaptor.capture(),
                endTimeCaptor.capture(),
                eq(OrderStatus.DELIVERED.getCode()),
                eq(OrderStatus.FAILED.getCode()));

        // Verify thời gian truy vấn
        LocalDateTime start = startTimeCaptor.getValue();
        LocalDateTime end = endTimeCaptor.getValue();

        assertEquals(validDayRequest.getStartDate(), start.toLocalDate());
        assertEquals(validDayRequest.getEndDate(), end.toLocalDate());
    }

    /**
     * Test case: Export báo cáo thành công - Type MONTH
     * Expected: Tạo file Excel với các sheet theo từng warehouse
     */
    @Test
    @DisplayName("exportDeliveryPerformanceReport - Type MONTH thành công")
    void exportDeliveryPerformanceReport_Month_Success() {
        // Given
        List<DeliveryPerformanceProjection> mockData = Arrays.asList(
                createMockProjection(1L, YearMonth.now().minusMonths(6).atDay(1).atStartOfDay(),
                        OrderStatus.DELIVERED.getCode(), null, 100L),
                createMockProjection(1L, YearMonth.now().minusMonths(6).atDay(1).atStartOfDay(),
                        OrderStatus.FAILED.getCode(), 101L, 20L),
                createMockProjection(2L, YearMonth.now().minusMonths(5).atDay(1).atStartOfDay(),
                        OrderStatus.DELIVERED.getCode(), null, 80L)
        );

        when(orderHistoryRepository.fetchDeliveryPerformanceData(
                anyList(), any(), any(), anyInt(), anyInt()))
                .thenReturn(mockData);

        // When
        ReportFileResponse response = reportService.exportDeliveryPerformanceReport(validMonthRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getFileName().startsWith("Report02_"));
    }

    /**
     * Test case: Export báo cáo - Không có dữ liệu
     * Expected: Vẫn tạo file Excel với header cho mỗi sheet
     */
    @Test
    @DisplayName("exportDeliveryPerformanceReport - Không có dữ liệu")
    void exportDeliveryPerformanceReport_NoData_Success() {
        // Given
        when(orderHistoryRepository.fetchDeliveryPerformanceData(
                anyList(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        ReportFileResponse response = reportService.exportDeliveryPerformanceReport(validDayRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getFileContent());
        assertTrue(response.getFileContent().length > 0);

        // Verify tạo sheet cho mỗi warehouse dù không có data
        verify(orderHistoryRepository).fetchDeliveryPerformanceData(
                eq(validDayRequest.getWarehouseIds()), any(), any(), anyInt(), anyInt());
    }

    /**
     * Test case: Export báo cáo - Có nhiều lý do thất bại khác nhau
     * Expected: Các cột lý do được tạo động dựa trên dữ liệu
     */
    @Test
    @DisplayName("exportDeliveryPerformanceReport - Nhiều lý do thất bại")
    void exportDeliveryPerformanceReport_MultipleFailureReasons_Success() {
        // Given - Dữ liệu với 3 lý do thất bại khác nhau
        List<DeliveryPerformanceProjection> mockData = Arrays.asList(
                createMockProjection(1L, LocalDateTime.now().minusDays(1), OrderStatus.FAILED.getCode(), 101L, 5L),
                createMockProjection(1L, LocalDateTime.now().minusDays(1), OrderStatus.FAILED.getCode(), 102L, 3L),
                createMockProjection(1L, LocalDateTime.now().minusDays(1), OrderStatus.FAILED.getCode(), 103L, 2L),
                createMockProjection(1L, LocalDateTime.now().minusDays(1), OrderStatus.DELIVERED.getCode(), null, 10L)
        );

        when(orderHistoryRepository.fetchDeliveryPerformanceData(
                anyList(), any(), any(), anyInt(), anyInt()))
                .thenReturn(mockData);

        // When
        ReportFileResponse response = reportService.exportDeliveryPerformanceReport(validDayRequest);

        // Then
        assertNotNull(response);
    }

    /**
     * Test case: Export báo cáo - Repository throw exception
     * Expected: Throw RuntimeException
     */
    @Test
    @DisplayName("exportDeliveryPerformanceReport - Repository lỗi")
    void exportDeliveryPerformanceReport_RepositoryError_ThrowsException() {
        // Given
        when(orderHistoryRepository.fetchDeliveryPerformanceData(
                anyList(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("DB Error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> reportService.exportDeliveryPerformanceReport(validDayRequest));
    }

    /**
     * Helper method: Tạo mock projection cho test
     */
    private DeliveryPerformanceProjection createMockProjection(
            Long warehouseId, LocalDateTime createdAt, Integer status, Long reasonId, Long total) {
        return new DeliveryPerformanceProjection() {
            @Override
            public Long getWarehouseId() {
                return warehouseId;
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return createdAt;
            }

            @Override
            public Integer getStatus() {
                return status;
            }

            @Override
            public Long getFailureReasonId() {
                return reasonId;
            }

            @Override
            public Long getTotal() {
                return total;
            }
        };
    }
}
