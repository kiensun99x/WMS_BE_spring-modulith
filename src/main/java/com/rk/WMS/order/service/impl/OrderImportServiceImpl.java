package com.rk.WMS.order.service.impl;

import com.rk.WMS.order.dto.request.CreateOrderRequest;
import com.rk.WMS.order.service.OrderImportService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
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

  /**
   * Đọc file excel, validate, ghi lỗi(nếu có) và trả về danh sách CreateOrderRequest
   * @param file file excel
   * @return danh sách CreateOrderRequest
   * @throws IOException
   */
  private List<CreateOrderRequest> readFile(MultipartFile file) throws IOException {
    List<CreateOrderRequest> result = new ArrayList<>();
    Workbook workbook = new XSSFWorkbook(file.getInputStream());
    Sheet sheet = workbook.getSheet("Orders");

    //đọc header

    Row headerRow = sheet.getRow(4);
    Map<String, Integer> columnIndexMap = new HashMap<>();
    DataFormatter formatter = new DataFormatter();

    for (Cell cell : headerRow) {
      columnIndexMap.put(cell.getStringCellValue(), cell.getColumnIndex());
    }

    //đọc data
    for (int i = 5; i <= sheet.getLastRowNum(); i++) {
      Row row = sheet.getRow(i);
      if (row == null) {
        continue;
      }
      CreateOrderRequest req = new CreateOrderRequest();

      req.setSupplierName(getString(row, columnIndexMap.get("supplierName")));
      req.setSupplierAddress(getString(row, columnIndexMap.get("supplierAddress")));
      req.setSupplierPhone(getString(row, columnIndexMap.get("supplierPhone")));
      req.setSupplierEmail(getString(row, columnIndexMap.get("supplierEmail")));

      req.setReceiverName(getString(row, columnIndexMap.get("receiverName")));
      req.setReceiverAddress(getString(row, columnIndexMap.get("receiverAddress")));
      req.setReceiverPhone(getString(row, columnIndexMap.get("receiverPhone")));
      req.setReceiverEmail(getString(row, columnIndexMap.get("receiverEmail")));

      req.setReceiverLat(getDouble(row, columnIndexMap.get("receiverLat")));
      req.setReceiverLon(getDouble(row, columnIndexMap.get("receiverLon")));

      Set<ConstraintViolation<CreateOrderRequest>> violations =
          validator.validate(req);

      if (!violations.isEmpty()) {
        String errorMessage = violations.stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .collect(Collectors.joining("; "));

        /**
         * TODO: lưu lỗi cho dòng i và rollback...
         */
      } else {
        result.add(req);
      }
    }
    return result;
  }

  /**
   * lấy giá trị của cell trong row
   * @param row
   * @param index
   * @return
   */
  private String getString(Row row, Integer index) {
    if (index == null) return null;
    Cell cell = row.getCell(index);
    if (cell == null) return null;
    cell.setCellType(CellType.STRING);
    return cell.getStringCellValue().trim();
  }

  /**
   * lấy giá trị của cell dạng số trong row
   * @param row
   * @param index
   * @return
   */
  private Double getDouble(Row row, Integer index) {
    if (index == null) return null;
    Cell cell = row.getCell(index);
    if (cell == null) return null;
    return cell.getNumericCellValue();
  }
}
