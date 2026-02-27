package com.rk.WMS.report.service;


import com.rk.WMS.report.dto.request.WarehouseOrderStatisticReportRequest;
import com.rk.WMS.report.dto.response.ReportFileResponse;

public interface WarehouseOrderStatisticReportService {

    ReportFileResponse exportWarehouseOrderStatisticReport(
            WarehouseOrderStatisticReportRequest request
    );
}
