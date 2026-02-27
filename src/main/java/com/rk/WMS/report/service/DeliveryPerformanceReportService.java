package com.rk.WMS.report.service;

import com.rk.WMS.report.dto.request.DeliveryPerformanceReportRequest;
import com.rk.WMS.report.dto.response.ReportFileResponse;

public interface DeliveryPerformanceReportService {

    ReportFileResponse exportDeliveryPerformanceReport(
            DeliveryPerformanceReportRequest request);
}

