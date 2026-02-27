package com.rk.WMS.report.dto.request;

import com.rk.WMS.common.constants.ReportType;
import lombok.Data;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Data
public class DeliveryPerformanceReportRequest {

    private List<Long> warehouseIds;

    private ReportType type;

    // DAY
    private LocalDate startDate;
    private LocalDate endDate;

    // MONTH
    private YearMonth startMonth;
    private YearMonth endMonth;
}