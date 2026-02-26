package com.rk.WMS.report.dto;


import com.rk.WMS.common.constants.ReportType;
import com.rk.WMS.report.dto.request.WarehouseOrderStatisticReportRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mục đích: Kiểm tra việc khởi tạo và getter/setter của DTO request
 */
class WarehouseOrderStatisticReportRequestTest {

    /**
     * Test case: Khởi tạo request bằng Builder
     * Expected: Tất cả fields được set đúng giá trị
     */
    @Test
    @DisplayName("Builder - Khởi tạo request thành công")
    void builder_CreateRequest_Success() {
        // Given
        ReportType type = ReportType.DAY;
        LocalDate startDate = LocalDate.of(2023, 12, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 15);
        YearMonth startMonth = YearMonth.of(2023, 12);
        YearMonth endMonth = YearMonth.of(2023, 12);
        var warehouseIds = Arrays.asList(1L, 2L, 3L);

        // When
        WarehouseOrderStatisticReportRequest request = WarehouseOrderStatisticReportRequest.builder()
                .type(type)
                .startDate(startDate)
                .endDate(endDate)
                .startMonth(startMonth)
                .endMonth(endMonth)
                .warehouseIds(warehouseIds)
                .build();

        // Then
        assertEquals(type, request.getType());
        assertEquals(startDate, request.getStartDate());
        assertEquals(endDate, request.getEndDate());
        assertEquals(startMonth, request.getStartMonth());
        assertEquals(endMonth, request.getEndMonth());
        assertEquals(warehouseIds, request.getWarehouseIds());
    }

    /**
     * Test case: Sử dụng NoArgsConstructor và Setter
     * Expected: Có thể set từng field sau khi khởi tạo
     */
    @Test
    @DisplayName("NoArgsConstructor và Setter - Khởi tạo request")
    void noArgsConstructorAndSetter_CreateRequest_Success() {
        // Given
        WarehouseOrderStatisticReportRequest request = new WarehouseOrderStatisticReportRequest();

        // When
        request.setType(ReportType.MONTH);
        request.setStartMonth(YearMonth.of(2023, 1));
        request.setEndMonth(YearMonth.of(2023, 12));
        request.setWarehouseIds(Arrays.asList(1L, 2L));

        // Then
        assertEquals(ReportType.MONTH, request.getType());
        assertEquals(YearMonth.of(2023, 1), request.getStartMonth());
        assertEquals(YearMonth.of(2023, 12), request.getEndMonth());
        assertEquals(2, request.getWarehouseIds().size());
    }

    /**
     * Test case: Sử dụng AllArgsConstructor
     * Expected: Khởi tạo với tất cả fields
     */
    @Test
    @DisplayName("AllArgsConstructor - Khởi tạo với tất cả fields")
    void allArgsConstructor_CreateRequest_Success() {
        // Given
        ReportType type = ReportType.DAY;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        YearMonth startMonth = YearMonth.now();
        YearMonth endMonth = YearMonth.now().plusMonths(1);
        var warehouseIds = Arrays.asList(1L, 2L);

        // When
        WarehouseOrderStatisticReportRequest request = new WarehouseOrderStatisticReportRequest(
                type, startDate, endDate, startMonth, endMonth, warehouseIds);

        // Then
        assertEquals(type, request.getType());
        assertEquals(startDate, request.getStartDate());
        assertEquals(endDate, request.getEndDate());
        assertEquals(startMonth, request.getStartMonth());
        assertEquals(endMonth, request.getEndMonth());
        assertEquals(warehouseIds, request.getWarehouseIds());
    }
}
