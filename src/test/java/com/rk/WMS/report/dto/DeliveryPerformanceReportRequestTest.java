package com.rk.WMS.report.dto;


import com.rk.WMS.common.constants.ReportType;
import com.rk.WMS.report.dto.request.DeliveryPerformanceReportRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mục đích: Kiểm tra việc khởi tạo và getter/setter của DTO request
 */
class DeliveryPerformanceReportRequestTest {

    /**
     * Test case: Khởi tạo request với các fields
     * Expected: Tất cả fields được set đúng giá trị
     */
    @Test
    @DisplayName("Getter/Setter - Khởi tạo request thành công")
    void getterSetter_CreateRequest_Success() {
        // Given
        DeliveryPerformanceReportRequest request = new DeliveryPerformanceReportRequest();

        // When
        request.setWarehouseIds(Arrays.asList(1L, 2L, 3L));
        request.setType(ReportType.DAY);
        request.setStartDate(LocalDate.of(2023, 12, 1));
        request.setEndDate(LocalDate.of(2023, 12, 15));
        request.setStartMonth(YearMonth.of(2023, 12));
        request.setEndMonth(YearMonth.of(2024, 1));

        // Then
        assertEquals(3, request.getWarehouseIds().size());
        assertEquals(ReportType.DAY, request.getType());
        assertEquals(LocalDate.of(2023, 12, 1), request.getStartDate());
        assertEquals(LocalDate.of(2023, 12, 15), request.getEndDate());
        assertEquals(YearMonth.of(2023, 12), request.getStartMonth());
        assertEquals(YearMonth.of(2024, 1), request.getEndMonth());
    }

    /**
     * Test case: Request với type = DAY
     * Expected: Các field liên quan đến ngày được set, field tháng có thể null
     */
    @Test
    @DisplayName("Type DAY - Chỉ set các field ngày")
    void typeDay_OnlyDateFieldsSet() {
        // Given
        DeliveryPerformanceReportRequest request = new DeliveryPerformanceReportRequest();

        // When
        request.setType(ReportType.DAY);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));
        request.setWarehouseIds(Arrays.asList(1L));

        // Then
        assertEquals(ReportType.DAY, request.getType());
        assertNotNull(request.getStartDate());
        assertNotNull(request.getEndDate());
        assertNull(request.getStartMonth());
        assertNull(request.getEndMonth());
    }

    /**
     * Test case: Request với type = MONTH
     * Expected: Các field liên quan đến tháng được set, field ngày có thể null
     */
    @Test
    @DisplayName("Type MONTH - Chỉ set các field tháng")
    void typeMonth_OnlyMonthFieldsSet() {
        // Given
        DeliveryPerformanceReportRequest request = new DeliveryPerformanceReportRequest();

        // When
        request.setType(ReportType.MONTH);
        request.setStartMonth(YearMonth.now().minusMonths(6));
        request.setEndMonth(YearMonth.now());
        request.setWarehouseIds(Arrays.asList(1L));

        // Then
        assertEquals(ReportType.MONTH, request.getType());
        assertNotNull(request.getStartMonth());
        assertNotNull(request.getEndMonth());
        assertNull(request.getStartDate());
        assertNull(request.getEndDate());
    }
}
