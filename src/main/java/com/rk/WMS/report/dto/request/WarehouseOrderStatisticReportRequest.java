package com.rk.WMS.report.dto.request;


import com.rk.WMS.common.constants.ReportType;
import lombok.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseOrderStatisticReportRequest {

    private ReportType type; // DAY or MONTH

    // DAY
    private LocalDate startDate;
    private LocalDate endDate;

    // MONTH (format: yyyy-MM)
    private YearMonth startMonth;
    private YearMonth endMonth;


    private List<Long> warehouseIds;
}