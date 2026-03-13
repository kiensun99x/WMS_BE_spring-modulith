package com.rk.WMS.report.service.Impl;

import com.rk.WMS.common.constants.DateTimePattern;
import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.common.constants.ReportType;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.history.service.OrderHistoryQueryService;
import com.rk.WMS.report.dto.request.DeliveryPerformanceReportRequest;
import com.rk.WMS.report.dto.response.ReportFileResponse;
import com.rk.WMS.report.projection.DeliveryPerformanceProjection;
import com.rk.WMS.report.service.DeliveryPerformanceReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j(topic = "DELIVERY-PERFORMANCE-SERVICE")
@Service
@RequiredArgsConstructor
public class DeliveryPerformanceReportServiceImpl
        implements DeliveryPerformanceReportService {

    private final OrderHistoryQueryService orderHistoryQueryService;

    /**
     * Xuất báo cáo hiệu suất giao hàng.
     *
     * @param request Thông tin filter báo cáo (warehouse, thời gian, type DAY/MONTH)
     * @return ReportFileResponse chứa tên file và nội dung file Excel dạng byte[]
     */
    @Override
    public ReportFileResponse exportDeliveryPerformanceReport(
            DeliveryPerformanceReportRequest request) {

        log.info("[DELIVERY_PERFORMANCE_EXPORT_START] warehouseIds={}, type={}",
                request.getWarehouseIds(), request.getType());

        //  Validate request đầu vào
        validateRequest(request);

        LocalDateTime start;
        LocalDateTime end;

        // 2. Xác định khoảng thời gian truy vấn theo loại report
        if (request.getType() == ReportType.DAY) {
            start = request.getStartDate().atStartOfDay();
            end = request.getEndDate().atTime(23, 59, 59);
        } else {
            start = request.getStartMonth().atDay(1).atStartOfDay();
            end = request.getEndMonth().atEndOfMonth().atTime(23, 59, 59);
        }

        log.info("[DELIVERY_PERFORMANCE_QUERY] warehouseIds={}, from={}, to={}",
                request.getWarehouseIds(), start, end);

        // 3. Truy vấn dữ liệu từ DB
        List<DeliveryPerformanceProjection> rawData =
                orderHistoryQueryService.fetchDeliveryPerformanceData(
                        request.getWarehouseIds(),
                        start,
                        end,
                        OrderStatus.DELIVERED.getCode(),
                        OrderStatus.FAILED.getCode()
                );

        log.info("[DELIVERY_PERFORMANCE_QUERY_RESULT] warehouseIds={}, recordCount={}",
                request.getWarehouseIds(), rawData.size());

        // 4. Tạo file Excel
        byte[] fileBytes = generateExcel(rawData, request);

        String fileName = "Report02_" +
                LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern(DateTimePattern.FILE_TIMESTAMP))
                + ".xlsx";

        log.info("[DELIVERY_PERFORMANCE_EXPORT_SUCCESS] fileName={}, warehouseIds={}",
                fileName, request.getWarehouseIds());


        return ReportFileResponse.builder()
                .fileName(fileName)
                .fileContent(fileBytes)
                .build();
    }

    /**
     * Validate dữ liệu request.
     *
     * Rule:
     * - warehouseIds không được null / rỗng
     * - Tối đa 10 warehouse
     * - Nếu report DAY: tối đa 15 ngày
     * - Nếu report MONTH: tối đa 12 tháng
     */
    private void validateRequest(DeliveryPerformanceReportRequest request) {

        if (request.getWarehouseIds() == null || request.getWarehouseIds().isEmpty()) {
            throw new AppException(ErrorCode.MISSING_START_TIME);
        }

        if (request.getWarehouseIds().size() > 10) {
            throw new AppException(ErrorCode.MISSING_START_TIME);
        }

        if (request.getType() == ReportType.DAY) {

            long days = ChronoUnit.DAYS.between(
                    request.getStartDate(),
                    request.getEndDate());

            if (days > 15) {
                throw new AppException(ErrorCode.MISSING_END_TIME);
            }
        }

        if (request.getType() == ReportType.MONTH) {

            long months = ChronoUnit.MONTHS.between(
                    request.getStartMonth(),
                    request.getEndMonth());

            if (months > 12) {
                throw new AppException(ErrorCode.VALUE_EXCEED_LIMIT);
            }
        }
    }

    /**
     * Tạo file Excel từ dữ liệu truy vấn.
     *
     * Logic:
     * - Group dữ liệu theo warehouse
     * - Mỗi warehouse tạo 1 sheet
     * - Build nội dung sheet theo từng khoảng thời gian
     */
    private byte[] generateExcel(
            List<DeliveryPerformanceProjection> data,
            DeliveryPerformanceReportRequest request) {


        try (Workbook workbook = new XSSFWorkbook()) {

            // Group dữ liệu theo warehouseId
            Map<Long, List<DeliveryPerformanceProjection>> warehouseMap =
                    data.stream().collect(Collectors.groupingBy(
                            DeliveryPerformanceProjection::getWarehouseId));

            // Tạo sheet cho từng warehouse trong request
            for (Long warehouseId : request.getWarehouseIds()) {

                Sheet sheet = workbook.createSheet("WH_" + warehouseId);

                List<DeliveryPerformanceProjection> warehouseData =
                        warehouseMap.getOrDefault(warehouseId, new ArrayList<>());

                buildSheet(sheet, warehouseData, request);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Build nội dung cho từng sheet.
     *
     * Bao gồm:
     * - Header
     * - Dữ liệu theo từng period (ngày / tháng)
     * - Tổng cộng cuối bảng
     */
    private void buildSheet(
            Sheet sheet,
            List<DeliveryPerformanceProjection> data,
            DeliveryPerformanceReportRequest request) {

        // 1. Tạo header
        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("STT");
        header.createCell(1).setCellValue("Thời gian");
        header.createCell(2).setCellValue("Tổng đơn giao");
        header.createCell(3).setCellValue("Giao thành công");
        header.createCell(4).setCellValue("Giao thất bại");

        // 2. Xác định các failure reason xuất hiện trong dataset
        Set<Long> reasonIds = data.stream()
                .map(DeliveryPerformanceProjection::getFailureReasonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        int colIndex = 5;
        for (Long reasonId : reasonIds) {
            header.createCell(colIndex++)
                    .setCellValue("Lý do " + reasonId);
        }

        // 3. Group dữ liệu theo period (day hoặc month)
        Map<String, List<DeliveryPerformanceProjection>> periodMap =
                data.stream().collect(Collectors.groupingBy(
                        p -> formatPeriod(p.getCreatedAt(), request.getType()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        int rowIndex = 1;
        int stt = 1;

        long totalAll = 0;
        long successAll = 0;
        long failAll = 0;
        Map<Long, Long> totalReasonAll = new HashMap<>();

        // 4. Duyệt từng period để build row
        for (String period : periodMap.keySet()) {

            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(stt++);
            row.createCell(1).setCellValue(period);

            long success = 0;
            long fail = 0;
            Map<Long, Long> reasonMap = new HashMap<>();

            for (var p : periodMap.get(period)) {

                OrderStatus status = OrderStatus.fromCode(p.getStatus());

                if (status == OrderStatus.DELIVERED) {
                    success += p.getTotal();
                } else if (status == OrderStatus.FAILED) {
                    fail += p.getTotal();

                    if (p.getFailureReasonId() != null) {
                        reasonMap.merge(
                                p.getFailureReasonId(),
                                p.getTotal(),
                                Long::sum);
                    }
                }
            }

            row.createCell(2).setCellValue(success + fail);
            row.createCell(3).setCellValue(success);
            row.createCell(4).setCellValue(fail);

            totalAll += (success + fail);
            successAll += success;
            failAll += fail;

            int reasonCol = 5;
            for (Long reasonId : reasonIds) {

                long value = reasonMap.getOrDefault(reasonId, 0L);
                row.createCell(reasonCol++).setCellValue(value);

                totalReasonAll.merge(reasonId, value, Long::sum);
            }
        }

        // 5. Tạo dòng tính tổng
        Row totalRow = sheet.createRow(rowIndex);
        totalRow.createCell(1).setCellValue("Tổng");
        totalRow.createCell(2).setCellValue(totalAll);
        totalRow.createCell(3).setCellValue(successAll);
        totalRow.createCell(4).setCellValue(failAll);

        int reasonCol = 5;
        for (Long reasonId : reasonIds) {
            totalRow.createCell(reasonCol++)
                    .setCellValue(totalReasonAll.getOrDefault(reasonId, 0L));
        }
    }

    /**
     * Format thời gian theo loại report.
     *
     * - DAY   -> yyyy-MM-dd
     * - MONTH -> yyyy-MM
     */
    private String formatPeriod(LocalDateTime time, ReportType type) {
        if (type == ReportType.DAY) {
            return time.toLocalDate().toString();
        }
        return YearMonth.from(time).toString();
    }
}
