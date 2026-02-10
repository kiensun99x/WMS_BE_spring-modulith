package com.rk.WMS.order.service.impl;

import com.rk.WMS.order.dto.request.CreateOrderRequest;
import com.rk.WMS.order.service.OrderImportService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

  private List<CreateOrderRequest> readFile(MultipartFile file) throws IOException {
    List<CreateOrderRequest> result = new ArrayList<>();
    DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("vi-VN"));

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = workbook.getSheet("Orders");
      if (sheet == null) {
        throw new RuntimeException("Sheet 'Orders' not found");
      }

      int headerRowIndex = 4; // Excel row 5
      Row headerRow = sheet.getRow(headerRowIndex);
      if (headerRow == null) {
        throw new RuntimeException("Header row is null at index " + headerRowIndex);
      }

      //lấy header thành dạng map
      Map<String, Integer> columnIndexMap = new HashMap<>();
      for (Cell cell : headerRow) {
        if (cell == null) continue;
        String raw = formatter.formatCellValue(cell);
        String key = normalizeHeader(raw);
        if (!key.isBlank()) {
          columnIndexMap.put(key, cell.getColumnIndex());
        }
      }

      // Debug: nhìn thấy header thực tế sau normalize
      System.out.println("Header(normalized) = " + columnIndexMap.keySet());

      // Map theo tiếng Việt (dùng alias)
      Integer supplierNameCol = requireAnyColumn(columnIndexMap, "tenncc", "ten nha cung cap");
      Integer supplierAddressCol = requireAnyColumn(columnIndexMap, "diachincc", "dia chi ncc", "dia chi nha cung cap");
      Integer supplierPhoneCol = requireAnyColumn(columnIndexMap, "sdtncc", "sdt ncc", "so dien thoai ncc");
      Integer supplierEmailCol = requireAnyColumn(columnIndexMap, "emailncc", "email ncc");

      Integer receiverNameCol = requireAnyColumn(columnIndexMap, "tennguoinhan", "ten nguoi nhan");
      Integer receiverAddressCol = requireAnyColumn(columnIndexMap, "diachinguoinhan", "dia chi nguoi nhan");
      Integer receiverPhoneCol = requireAnyColumn(columnIndexMap, "sdtnguoinhan", "sdt nguoi nhan", "so dien thoai nguoi nhan");
      Integer receiverEmailCol = requireAnyColumn(columnIndexMap, "emailnguoinhan", "email nguoi nhan");

      Integer receiverLatCol = requireAnyColumn(columnIndexMap, "vidonguoinhan", "vi do nguoi nhan");
      Integer receiverLonCol = requireAnyColumn(columnIndexMap, "kinhdonguoinhan", "kinh do nguoi nhan");

      for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) continue;

        CreateOrderRequest req = new CreateOrderRequest();

        req.setSupplierName(getString(row, supplierNameCol, formatter));
        req.setSupplierAddress(getString(row, supplierAddressCol, formatter));
        req.setSupplierPhone(getString(row, supplierPhoneCol, formatter));
        req.setSupplierEmail(getString(row, supplierEmailCol, formatter));

        req.setReceiverName(getString(row, receiverNameCol, formatter));
        req.setReceiverAddress(getString(row, receiverAddressCol, formatter));
        req.setReceiverPhone(getString(row, receiverPhoneCol, formatter));
        req.setReceiverEmail(getString(row, receiverEmailCol, formatter));

        req.setReceiverLat(getDouble(row, receiverLatCol, formatter));
        req.setReceiverLon(getDouble(row, receiverLonCol, formatter));

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
   * Chuẩn hoá header để so khớp được nhiều kiểu viết khác nhau.
   *
   * Mục tiêu:
   * - Header trong Excel có thể là: "Kinh độ người nhận", "KINH ĐỘ NGƯỜI NHẬN", "Kinh do nguoi nhan"
   * - Ta muốn đưa về 1 dạng thống nhất: "kinhdonguoinhan"
   *
   * Vì sao cần Normalizer?
   * - Nếu dùng regex [^a-z0-9] trực tiếp thì chữ có dấu tiếng Việt sẽ bị loại bỏ => key bị sai
   */
  private String normalizeHeader(String s) {
    if (s == null) return "";

    // 1) Trim + lowercase để không phân biệt hoa/thường và khoảng trắng đầu/cuối
    String x = s.trim().toLowerCase(Locale.ROOT);

    // đưa về dạng không dấu
    x = Normalizer.normalize(x, Normalizer.Form.NFD)
        .replaceAll("\\p{M}+", ""); // bỏ dấu

    // xử lý riêng ký tự đ/Đ
    x = x.replace('đ', 'd').replace('Đ', 'd');

    // bỏ mọi ký tự không phải chữ/số
    x = x.replaceAll("[^a-z0-9]+", "");

    return x;
  }

  /**
   * Lấy index cột theo nhiều alias (tên gọi khác nhau) của cùng 1 cột.
   *
   * Ví dụ cột SĐT NCC có thể là:
   * - "SĐT NCC"
   * - "SDT NCC"
   * - "Số điện thoại NCC"
   *
   * Hàm này sẽ thử lần lượt các alias, alias nào tồn tại trong header map thì trả về index cột.
   * Nếu không thấy alias nào, throw lỗi để biết ngay thiếu cột gì (tránh import ra [] mà không rõ vì sao).
   */
  private Integer requireAnyColumn(Map<String, Integer> map, String... aliases) {
    for (String a : aliases) {
      String key = normalizeHeader(a);
      Integer idx = map.get(key);
      if (idx != null) return idx;
    }
    throw new RuntimeException("Missing required column. Tried: " + String.join(", ", aliases) + ". Found: " + map.keySet());
  }

  private String getString(Row row, Integer index, DataFormatter formatter) {
    if (index == null) return null;
    Cell cell = row.getCell(index);
    if (cell == null) return null;

    String v = formatter.formatCellValue(cell);
    if (v == null) return null;
    v = v.trim();
    return v.isBlank() ? null : v;
  }

  private Double getDouble(Row row, Integer index, DataFormatter formatter) {
    if (index == null) return null;
    Cell cell = row.getCell(index);
    if (cell == null) return null;

    String raw = formatter.formatCellValue(cell);
    if (raw == null) return null;
    raw = raw.trim();
    if (raw.isBlank()) return null;

    raw = raw.replace(",", ".");
    try {
      return Double.parseDouble(raw);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
