package com.rk.WMS.order.service.impl;

import com.rk.WMS.order.dto.request.CreateOrderRequest;
import com.rk.WMS.order.service.OrderImportService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class OrderImportServiceImpl implements OrderImportService {
  private static final String TEMPLATE_PATH = "template/importOrder/INB_ImportData.xlsx";
  private static final String FILE_NAME = "INB_ImportData.xlsx";

  private static final String SHEET_NAME = "Orders";
  private static final int START_ROW_DATA = 5;

  private static final int SUPPLIER_NAME_COL = 1;
  private static final int SUPPLIER_ADDRESS_COL = 2;
  private static final int SUPPLIER_PHONE_COL = 3;
  private static final int SUPPLIER_EMAIL_COL = 4;
  private static final int RECEIVER_NAME_COL = 5;
  private static final int RECEIVER_ADDRESS_COL = 6;
  private static final int RECEIVER_PHONE_COL = 7;
  private static final int RECEIVER_EMAIL_COL = 8;
  private static final int RECEIVER_LAT_COL = 9;
  private static final int RECEIVER_LON_COL = 10;

  private final Validator validator;
  private final OrderServiceImpl orderService;

  @Override
  public ResponseEntity<Resource> downloadTemplate() {
    ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);

    if (!resource.exists()) {
      throw new RuntimeException("Template file not found at classpath:" + TEMPLATE_PATH);
    }

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ))
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + FILE_NAME + "\"")
        .body(resource);
  }

  @Override
  public void importExcel(MultipartFile file) throws IOException {
    List<CreateOrderRequest> createOrderRequestList = readFile(file);
    //create order
    for (CreateOrderRequest req : createOrderRequestList) {
      orderService.createOrder(req);
    }
  }

  private List<CreateOrderRequest> readFile(MultipartFile file) throws IOException {
    List<CreateOrderRequest> result = new ArrayList<>();
    DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("vi-VN"));

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = workbook.getSheet(SHEET_NAME);
      if (sheet == null) {
        throw new RuntimeException("Sheet 'Orders' not found");
      }

      for (int i = START_ROW_DATA; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) continue;

        CreateOrderRequest req = new CreateOrderRequest();

        req.setSupplierName(getString(row, SUPPLIER_NAME_COL, formatter));
        req.setSupplierAddress(getString(row, SUPPLIER_ADDRESS_COL, formatter));
        req.setSupplierPhone(getString(row, SUPPLIER_PHONE_COL, formatter));
        req.setSupplierEmail(getString(row, SUPPLIER_EMAIL_COL, formatter));

        req.setReceiverName(getString(row, RECEIVER_NAME_COL, formatter));
        req.setReceiverAddress(getString(row, RECEIVER_ADDRESS_COL, formatter));
        req.setReceiverPhone(getString(row, RECEIVER_PHONE_COL, formatter));
        req.setReceiverEmail(getString(row, RECEIVER_EMAIL_COL, formatter));

        req.setReceiverLat(getDouble(row, RECEIVER_LAT_COL));
        req.setReceiverLon(getDouble(row, RECEIVER_LON_COL));

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(req);

        if (!violations.isEmpty()) {
          String errorMessage = violations.stream()
              .map(v -> v.getPropertyPath() + ": " + v.getMessage())
              .collect(Collectors.joining("; "));
          System.out.println("Row " + (i + 1) + " invalid: " + errorMessage);
        } else {
          result.add(req);
        }
      }

      System.out.println("Valid rows imported: " + result.size());
      return result;
    }
  }


  /**
   *
   * @return trả về null nếu cell không có giá trị hoặc giá trị rỗng
   */
  private String getString(Row row, Integer index, DataFormatter formatter) {
    if (index == null) return null;
    Cell cell = row.getCell(index);
    if (cell == null) return null;

    String value = formatter.formatCellValue(cell);
    if (value == null) return null;
    value = value.trim();
    return value.isBlank() ? null : value;
  }

  /**
   *
   * @return trả về null nếu cell không có giá trị
   */
  private Double getDouble(Row row, Integer index) {
    if (index == null) return null;
    Cell cell = row.getCell(index);
    if (cell == null) return null;
    return cell.getNumericCellValue();
  }
}
