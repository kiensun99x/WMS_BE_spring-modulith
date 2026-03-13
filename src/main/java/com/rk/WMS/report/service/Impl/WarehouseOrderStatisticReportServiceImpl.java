package com.rk.WMS.report.service.Impl;


import com.rk.WMS.common.constants.DateTimePattern;
import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.common.constants.ReportType;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.history.service.OrderHistoryQueryService;
import com.rk.WMS.report.dto.request.WarehouseOrderStatisticReportRequest;
import com.rk.WMS.report.dto.response.ReportFileResponse;
import com.rk.WMS.report.service.WarehouseOrderStatisticReportService;
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

@Slf4j(topic = "ORDER-STATISTIC-SERVICE")
@Service
@RequiredArgsConstructor
public class WarehouseOrderStatisticReportServiceImpl
        implements WarehouseOrderStatisticReportService {

    private final OrderHistoryQueryService orderHistoryQueryService;

    /**
     * Xuất báo cáo thống kê đơn theo kho.
     *
     * @param request Thông tin filter báo cáo (warehouseIds, thời gian, type DAY/MONTH)
     * @return ReportFileResponse chứa tên file và nội dung file Excel
     */
    @Override
    public ReportFileResponse exportWarehouseOrderStatisticReport(
            WarehouseOrderStatisticReportRequest request) {

        log.info("[WAREHOUSE_STATISTIC_EXPORT_START] warehouseIds={}, type={}",
                request != null ? request.getWarehouseIds() : null,
                request != null ? request.getType() : null);

        // 1. Validate request
        validateRequest(request);

        LocalDateTime start;
        LocalDateTime end;

        // 2. Xác định khoảng thời gian truy vấn theo loại report
        if (request.getType() == ReportType.DAY) {

            start = request.getStartDate().atStartOfDay();
            end = request.getEndDate().atTime(23, 59, 59);

        } else { // MONTH

            YearMonth startYM = request.getStartMonth();
            YearMonth endYM = request.getEndMonth();

            start = startYM.atDay(1).atStartOfDay();
            end = endYM.atEndOfMonth().atTime(23, 59, 59);
        }

        // 3. Truy vấn dữ liệu
        List<Object[]> rawData = orderHistoryQueryService.fetchStatisticWarehouseData(
                OrderStatus.STORED.getCode(),
                request.getWarehouseIds(),
                start,
                end
        );

        log.info("[WAREHOUSE_STATISTIC_QUERY_RESULT] warehouseIds={}, recordCount={}",
                request.getWarehouseIds(), rawData.size());


        // 4. Tạo file Excel
        byte[] file = generateExcel(rawData, request);

        String fileName = "Report01_" +
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(DateTimePattern.FILE_TIMESTAMP))
                + ".xlsx";

        log.info("[WAREHOUSE_STATISTIC_EXPORT_SUCCESS] fileName={}, warehouseIds={}",
                fileName, request.getWarehouseIds());

        return ReportFileResponse.builder()
                .fileName(fileName)
                .fileContent(file)
                .build();
    }


    /**
     * Validate dữ liệu đầu vào.
     *
     * Rule:
     * - request không được null
     * - warehouseIds không được rỗng
     * - tối đa 10 warehouse
     * - DAY: tối đa 15 ngày
     * - MONTH: tối đa 12 tháng
     */
    private void validateRequest(WarehouseOrderStatisticReportRequest request) {

        if (request == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        if (request.getWarehouseIds() == null
                || request.getWarehouseIds().isEmpty()) {
            throw new AppException(ErrorCode.NO_AVAILABLE_WAREHOUSE);
        }

        if (request.getWarehouseIds().size() > 10) {
            throw new AppException(ErrorCode.VALUE_EXCEED_LIMIT);
        }

        if (request.getType() == ReportType.DAY) {

            if (request.getStartDate() == null)
                throw new AppException(ErrorCode.MISSING_START_TIME);

            if (request.getEndDate() == null)
                throw new AppException(ErrorCode.MISSING_END_TIME);

            if (request.getEndDate().isBefore(request.getStartDate()))
                throw new AppException(ErrorCode.INVALID_FORMAT);

            long days = ChronoUnit.DAYS.between(
                    request.getStartDate(),
                    request.getEndDate());

            if (days > 15)
                throw new AppException(ErrorCode.VALUE_EXCEED_LIMIT);
        }

        if (request.getType() == ReportType.MONTH) {

            if (request.getStartMonth() == null)
                throw new AppException(ErrorCode.MISSING_START_TIME);

            if (request.getEndMonth() == null)
                throw new AppException(ErrorCode.MISSING_END_TIME);

            YearMonth start = request.getStartMonth();
            YearMonth end = request.getEndMonth();

            if (end.isBefore(start))
                throw new AppException(ErrorCode.INVALID_FORMAT);

            long months = ChronoUnit.MONTHS.between(start, end);

            if (months > 12)
                throw new AppException(ErrorCode.VALUE_EXCEED_LIMIT);
        }
    }

    /**
     * Sinh file Excel từ dữ liệu raw query.
     *
     * Cấu trúc rawData:
     * [warehouseId, time, total]
     *
     * Output:
     * - Mỗi dòng tương ứng 1 khoảng thời gian
     * - Mỗi cột là 1 warehouse
     * - Có thêm cột tổng theo dòng
     * - Có dòng tổng cuối bảng
     */
    private byte[] generateExcel(List<Object[]> rawData,
                                 WarehouseOrderStatisticReportRequest request) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("BaoCao");

            // 1. Tạo header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("STT");
            header.createCell(1).setCellValue("Thời gian");

            List<Long> warehouseIds = request.getWarehouseIds();

            for (int i = 0; i < warehouseIds.size(); i++) {
                header.createCell(i + 2)
                        .setCellValue("Kho " + warehouseIds.get(i));
            }

            header.createCell(warehouseIds.size() + 2)
                    .setCellValue("Tổng cộng");

            Map<String, Map<Long, Long>> grouped = new LinkedHashMap<>();

            // 2. Chuẩn hóa dữ liệu từ rawData
            for (Object[] row : rawData) {

                // Giả định query trả: [time, warehouseId, total]
                Object warehouseObj = row[0];
                Object timeObj = row[1];
                Object totalObj = row[2];

                String timeKey;

                if (timeObj instanceof LocalDate) {
                    timeKey = ((LocalDate) timeObj).toString();
                } else if (timeObj instanceof LocalDateTime) {
                    timeKey = ((LocalDateTime) timeObj).toLocalDate().toString();
                } else {
                    timeKey = timeObj.toString();
                }

                Long warehouseId = ((Number) warehouseObj).longValue();
                Long total = ((Number) totalObj).longValue();

                grouped
                        .computeIfAbsent(timeKey, k -> new HashMap<>())
                        .put(warehouseId, total);
            }

            int rowIndex = 1;
            int stt = 1;

            Map<Long, Long> totalPerWarehouse = new HashMap<>();

            // 3. Build từng dòng theo thời gian
            for (Map.Entry<String, Map<Long, Long>> entry : grouped.entrySet()) {

                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(entry.getKey());

                long totalRow = 0;

                for (int i = 0; i < warehouseIds.size(); i++) {

                    Long warehouseId = warehouseIds.get(i);
                    Long value = entry.getValue()
                            .getOrDefault(warehouseId, 0L);

                    row.createCell(i + 2).setCellValue(value);

                    totalRow += value;

                    totalPerWarehouse.merge(warehouseId,
                            value,
                            Long::sum);
                }

                // Tổng theo dòng (theo thời gian)
                row.createCell(warehouseIds.size() + 2)
                        .setCellValue(totalRow);
            }

            // 4. Dòng tổng cuối bảng
            Row totalRow = sheet.createRow(rowIndex);
            totalRow.createCell(1).setCellValue("Tổng");

            long grandTotal = 0;

            for (int i = 0; i < warehouseIds.size(); i++) {

                Long warehouseId = warehouseIds.get(i);
                Long total = totalPerWarehouse.getOrDefault(warehouseId, 0L);

                totalRow.createCell(i + 2).setCellValue(total);

                grandTotal += total;
            }

            totalRow.createCell(warehouseIds.size() + 2)
                    .setCellValue(grandTotal);

            // Auto size cột
            for (int i = 0; i <= warehouseIds.size() + 2; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating excel", e);
        }
    }
}