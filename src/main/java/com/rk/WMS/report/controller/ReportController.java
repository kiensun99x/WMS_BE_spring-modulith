package com.rk.WMS.report.controller;

import com.rk.WMS.report.dto.request.DeliveryPerformanceReportRequest;
import com.rk.WMS.report.dto.request.WarehouseOrderStatisticReportRequest;
import com.rk.WMS.report.service.DeliveryPerformanceReportService;
import com.rk.WMS.report.service.WarehouseOrderStatisticReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final DeliveryPerformanceReportService deliveryPerformanceReportService;
    private final WarehouseOrderStatisticReportService warehouseOrderStatisticReportService;

    @PostMapping("/warehouse-order-statistic")
    public ResponseEntity<byte[]> exportWarehouseOrderStatistic(
            @RequestBody WarehouseOrderStatisticReportRequest request) {

        var result = warehouseOrderStatisticReportService
                .exportWarehouseOrderStatisticReport(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                        .filename(result.getFileName())
                        .build()
        );

        return new ResponseEntity<>(result.getFileContent(), headers, HttpStatus.OK);
    }


    @PostMapping("/delivery-performance")
    public ResponseEntity<byte[]> downloadDeliveryPerformanceReport(
            @RequestBody DeliveryPerformanceReportRequest request) {

        var result = deliveryPerformanceReportService.exportDeliveryPerformanceReport(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                        .filename(result.getFileName())
                        .build()
        );

        return new ResponseEntity<>(result.getFileContent(), headers, HttpStatus.OK);
    }
}
