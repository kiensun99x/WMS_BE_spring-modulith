package com.rk.WMS.report;

import com.rk.WMS.common.constants.ReportType;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.history.repository.OrderHistoryRepository;
import com.rk.WMS.report.dto.request.WarehouseOrderStatisticReportRequest;
import com.rk.WMS.report.dto.response.ReportFileResponse;
import com.rk.WMS.report.service.Impl.WarehouseOrderStatisticReportServiceImpl;
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
 * Mục đích: Kiểm tra business logic của service xuất báo cáo thống kê đơn theo kho
 */
@ExtendWith(MockitoExtension.class)
class WarehouseOrderStatisticReportServiceImplTest {

    @Mock
    private OrderHistoryRepository orderHistoryRepository;

    @InjectMocks
    private WarehouseOrderStatisticReportServiceImpl reportService;

    @Captor
    private ArgumentCaptor<LocalDateTime> startTimeCaptor;

    @Captor
    private ArgumentCaptor<LocalDateTime> endTimeCaptor;

    private WarehouseOrderStatisticReportRequest validDayRequest;
    private WarehouseOrderStatisticReportRequest validMonthRequest;

    /**
     * Setup trước mỗi test case
     * Khởi tạo các request hợp lệ
     */
    @BeforeEach
    void setUp() {
        // Request DAY hợp lệ: 7 ngày, 3 warehouses
        validDayRequest = WarehouseOrderStatisticReportRequest.builder()
                .type(ReportType.DAY)
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now())
                .warehouseIds(Arrays.asList(1L, 2L, 3L))
                .build();

        // Request MONTH hợp lệ: 6 tháng, 3 warehouses
        validMonthRequest = WarehouseOrderStatisticReportRequest.builder()
                .type(ReportType.MONTH)
                .startMonth(YearMonth.now().minusMonths(6))
                .endMonth(YearMonth.now())
                .warehouseIds(Arrays.asList(1L, 2L, 3L))
                .build();
    }

    // ==================== VALIDATION TESTS ====================

    /**
     * Test case: Validate request - Request null
     * Expected: Throw AppException với code VALIDATION_ERROR (SYSS-0008)
     */
    @Test
    @DisplayName("validateRequest - Request null -> SYSS-0008")
    void validateRequest_NullRequest_ThrowsException() {
        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportWarehouseOrderStatisticReport(null));

        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getErrorCode());
        verify(orderHistoryRepository, never()).fetchStatisticWarehouseData(any(), any(), any(), any());
    }

    /**
     * Test case: Validate request - WarehouseIds null
     * Expected: Throw AppException với code NO_AVAILABLE_WAREHOUSE (SYSS-0010)
     */
    @Test
    @DisplayName("validateRequest - WarehouseIds null -> SYSS-0010")
    void validateRequest_NullWarehouseIds_ThrowsException() {
        // Given
        validDayRequest.setWarehouseIds(null);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportWarehouseOrderStatisticReport(validDayRequest));

        assertEquals(ErrorCode.NO_AVAILABLE_WAREHOUSE, exception.getErrorCode());
    }

    /**
     * Test case: Validate request - WarehouseIds rỗng
     * Expected: Throw AppException với code NO_AVAILABLE_WAREHOUSE (SYSS-0010)
     */
    @Test
    @DisplayName("validateRequest - WarehouseIds rỗng -> SYSS-0010")
    void validateRequest_EmptyWarehouseIds_ThrowsException() {
        // Given
        validDayRequest.setWarehouseIds(Collections.emptyList());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportWarehouseOrderStatisticReport(validDayRequest));

        assertEquals(ErrorCode.NO_AVAILABLE_WAREHOUSE, exception.getErrorCode());
    }

    /**
     * Test case: Validate request - Quá 10 warehouse
     * Expected: Throw AppException với code VALUE_EXCEED_LIMIT (SYSS-0009)
     */
    @Test
    @DisplayName("validateRequest - Quá 10 warehouse -> SYSS-0009")
    void validateRequest_TooManyWarehouses_ThrowsException() {
        // Given - 11 warehouses
        validDayRequest.setWarehouseIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L));

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportWarehouseOrderStatisticReport(validDayRequest));

        assertEquals(ErrorCode.VALUE_EXCEED_LIMIT, exception.getErrorCode());
    }

    /**
     * Test case: Validate request DAY - Thiếu startDate
     * Expected: Throw AppException với code MISSING_START_TIME (SYSS-0008)
     */
    @Test
    @DisplayName("validateRequest DAY - Thiếu startDate -> SYSS-0008")
    void validateRequest_Day_MissingStartDate_ThrowsException() {
        // Given
        validDayRequest.setStartDate(null);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportWarehouseOrderStatisticReport(validDayRequest));

        assertEquals(ErrorCode.MISSING_START_TIME, exception.getErrorCode());
    }

    /**
     * Test case: Validate request DAY - Thiếu endDate
     * Expected: Throw AppException với code MISSING_END_TIME (SYSS-0008)
     */
    @Test
    @DisplayName("validateRequest DAY - Thiếu endDate -> SYSS-0008")
    void validateRequest_Day_MissingEndDate_ThrowsException() {
        // Given
        validDayRequest.setEndDate(null);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportWarehouseOrderStatisticReport(validDayRequest));

        assertEquals(ErrorCode.MISSING_END_TIME, exception.getErrorCode());
    }

    /**
     * Test case: Validate request DAY - endDate trước startDate
     * Expected: Throw AppException với code INVALID_FORMAT (SYSS-0008)
     */
    @Test
    @DisplayName("validateRequest DAY - endDate trước startDate -> SYSS-0008")
    void validateRequest_Day_EndDateBeforeStartDate_ThrowsException() {
        // Given
        validDayRequest.setStartDate(LocalDate.now());
        validDayRequest.setEndDate(LocalDate.now().minusDays(1));

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportWarehouseOrderStatisticReport(validDayRequest));

        assertEquals(ErrorCode.INVALID_FORMAT, exception.getErrorCode());
    }

    /**
     * Test case: Validate request DAY - Khoảng cách > 15 ngày
     * Expected: Throw AppException với code VALUE_EXCEED_LIMIT (SYSS-0009)
     */
    @Test
    @DisplayName("validateRequest DAY - > 15 ngày -> SYSS-0009")
    void validateRequest_Day_MoreThan15Days_ThrowsException() {
        // Given - 16 ngày
        validDayRequest.setStartDate(LocalDate.now().minusDays(16));
        validDayRequest.setEndDate(LocalDate.now());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportWarehouseOrderStatisticReport(validDayRequest));

        assertEquals(ErrorCode.VALUE_EXCEED_LIMIT, exception.getErrorCode());
    }

    /**
     * Test case: Validate request DAY - Đúng 15 ngày (boundary)
     * Expected: Thành công, không throw exception
     */
    @Test
    @DisplayName("validateRequest DAY - Đúng 15 ngày (boundary)")
    void validateRequest_Day_Exactly15Days_Success() {
        // Given - 15 ngày
        validDayRequest.setStartDate(LocalDate.now().minusDays(15));
        validDayRequest.setEndDate(LocalDate.now());

        // Mock data
        when(orderHistoryRepository.fetchStatisticWarehouseData(anyInt(), anyList(), any(), any()))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertDoesNotThrow(() -> reportService.exportWarehouseOrderStatisticReport(validDayRequest));
    }

    /**
     * Test case: Validate request MONTH - Thiếu startMonth
     * Expected: Throw AppException với code MISSING_START_TIME (SYSS-0008)
     */
    @Test
    @DisplayName("validateRequest MONTH - Thiếu startMonth -> SYSS-0008")
    void validateRequest_Month_MissingStartMonth_ThrowsException() {
        // Given
        validMonthRequest.setStartMonth(null);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportWarehouseOrderStatisticReport(validMonthRequest));

        assertEquals(ErrorCode.MISSING_START_TIME, exception.getErrorCode());
    }

    /**
     * Test case: Validate request MONTH - Thiếu endMonth
     * Expected: Throw AppException với code MISSING_END_TIME (SYSS-0008)
     */
    @Test
    @DisplayName("validateRequest MONTH - Thiếu endMonth -> SYSS-0008")
    void validateRequest_Month_MissingEndMonth_ThrowsException() {
        // Given
        validMonthRequest.setEndMonth(null);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportWarehouseOrderStatisticReport(validMonthRequest));

        assertEquals(ErrorCode.MISSING_END_TIME, exception.getErrorCode());
    }

    /**
     * Test case: Validate request MONTH - endMonth trước startMonth
     * Expected: Throw AppException với code INVALID_FORMAT (SYSS-0008)
     */
    @Test
    @DisplayName("validateRequest MONTH - endMonth trước startMonth -> SYSS-0008")
    void validateRequest_Month_EndMonthBeforeStartMonth_ThrowsException() {
        // Given
        validMonthRequest.setStartMonth(YearMonth.now());
        validMonthRequest.setEndMonth(YearMonth.now().minusMonths(1));

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> reportService.exportWarehouseOrderStatisticReport(validMonthRequest));

        assertEquals(ErrorCode.INVALID_FORMAT, exception.getErrorCode());
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
                () -> reportService.exportWarehouseOrderStatisticReport(validMonthRequest));

        assertEquals(ErrorCode.VALUE_EXCEED_LIMIT, exception.getErrorCode());
    }

    /**
     * Test case: Validate request MONTH - Đúng 12 tháng (boundary)
     * Expected: Thành công, không throw exception
     */
    @Test
    @DisplayName("validateRequest MONTH - Đúng 12 tháng (boundary)")
    void validateRequest_Month_Exactly12Months_Success() {
        // Given - 12 tháng
        validMonthRequest.setStartMonth(YearMonth.now().minusMonths(12));
        validMonthRequest.setEndMonth(YearMonth.now());

        // Mock data
        when(orderHistoryRepository.fetchStatisticWarehouseData(anyInt(), anyList(), any(), any()))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertDoesNotThrow(() -> reportService.exportWarehouseOrderStatisticReport(validMonthRequest));
    }

    // ==================== EXPORT TESTS ====================

    /**
     * Test case: Export báo cáo thành công - Type DAY
     * Expected:
     * - Tạo file Excel với tên đúng format: Report01_yyyyMMddHHMMSS.xlsx
     * - Gọi repository với tham số đúng
     */
    @Test
    @DisplayName("exportWarehouseOrderStatisticReport - Type DAY thành công")
    void exportWarehouseOrderStatisticReport_Day_Success() {
        // Given
        List<Object[]> mockData = Arrays.asList(
                new Object[]{1L, LocalDate.now().minusDays(7), 10L},
                new Object[]{2L, LocalDate.now().minusDays(7), 15L},
                new Object[]{1L, LocalDate.now().minusDays(6), 20L}
        );

        when(orderHistoryRepository.fetchStatisticWarehouseData(
                eq(3), // OrderStatus.STORED.getCode() = 3
                eq(validDayRequest.getWarehouseIds()),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(mockData);

        // When
        ReportFileResponse response = reportService.exportWarehouseOrderStatisticReport(validDayRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getFileName());
        assertTrue(response.getFileName().startsWith("Report01_"));
        assertTrue(response.getFileName().endsWith(".xlsx"));
        assertNotNull(response.getFileContent());
        assertTrue(response.getFileContent().length > 0);

        verify(orderHistoryRepository).fetchStatisticWarehouseData(
                eq(3),
                eq(validDayRequest.getWarehouseIds()),
                startTimeCaptor.capture(),
                endTimeCaptor.capture());

        // Verify thời gian truy vấn
        LocalDateTime start = startTimeCaptor.getValue();
        LocalDateTime end = endTimeCaptor.getValue();

        assertEquals(validDayRequest.getStartDate(), start.toLocalDate());
        assertEquals(validDayRequest.getEndDate(), end.toLocalDate());
        assertEquals(23, end.getHour());
        assertEquals(59, end.getMinute());
        assertEquals(59, end.getSecond());
    }

    /**
     * Test case: Export báo cáo thành công - Type MONTH
     * Expected:
     * - Tạo file Excel với tên đúng format
     * - Gọi repository với tham số đúng
     */
    @Test
    @DisplayName("exportWarehouseOrderStatisticReport - Type MONTH thành công")
    void exportWarehouseOrderStatisticReport_Month_Success() {
        // Given
        List<Object[]> mockData = Arrays.asList(
                new Object[]{1L, YearMonth.now().minusMonths(6).atDay(1), 50L},
                new Object[]{2L, YearMonth.now().minusMonths(6).atDay(1), 45L},
                new Object[]{1L, YearMonth.now().minusMonths(5).atDay(1), 60L}
        );

        when(orderHistoryRepository.fetchStatisticWarehouseData(
                eq(3),
                eq(validMonthRequest.getWarehouseIds()),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(mockData);

        // When
        ReportFileResponse response = reportService.exportWarehouseOrderStatisticReport(validMonthRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getFileName().startsWith("Report01_"));

        verify(orderHistoryRepository).fetchStatisticWarehouseData(
                eq(3),
                eq(validMonthRequest.getWarehouseIds()),
                startTimeCaptor.capture(),
                endTimeCaptor.capture());

        LocalDateTime start = startTimeCaptor.getValue();
        LocalDateTime end = endTimeCaptor.getValue();

        assertEquals(validMonthRequest.getStartMonth().atDay(1), start.toLocalDate());
        assertEquals(validMonthRequest.getEndMonth().atEndOfMonth(), end.toLocalDate());
    }

    /**
     * Test case: Export báo cáo - Không có dữ liệu
     * Expected: Vẫn tạo file Excel với header và dòng tổng = 0
     */
    @Test
    @DisplayName("exportWarehouseOrderStatisticReport - Không có dữ liệu")
    void exportWarehouseOrderStatisticReport_NoData_Success() {
        // Given
        when(orderHistoryRepository.fetchStatisticWarehouseData(anyInt(), anyList(), any(), any()))
                .thenReturn(Collections.emptyList());

        // When
        ReportFileResponse response = reportService.exportWarehouseOrderStatisticReport(validDayRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getFileContent());
        assertTrue(response.getFileContent().length > 0); // File vẫn được tạo với header
    }

    /**
     * Test case: Export báo cáo - Repository throw exception
     * Expected: Throw RuntimeException
     */
    @Test
    @DisplayName("exportWarehouseOrderStatisticReport - Repository lỗi")
    void exportWarehouseOrderStatisticReport_RepositoryError_ThrowsException() {
        // Given
        when(orderHistoryRepository.fetchStatisticWarehouseData(anyInt(), anyList(), any(), any()))
                .thenThrow(new RuntimeException("DB Error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> reportService.exportWarehouseOrderStatisticReport(validDayRequest));
    }
}