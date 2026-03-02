package com.rk.WMS.order.service.impl;

import static com.rk.WMS.common.constants.ExcelFilePattern.ERROR_SHEET_NAME;
import static com.rk.WMS.common.constants.ExcelFilePattern.EXCEL_FILE_FORMAT;
import static com.rk.WMS.common.constants.ExcelFilePattern.SHEET_NAME;

import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.common.currentUser.CurrentUserProvider;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.order.dto.request.CreateOrderRequest;
import com.rk.WMS.order.dto.response.OrderImportResponse;
import com.rk.WMS.order.mapper.OrderMapper;
import com.rk.WMS.order.model.ErrorFileImport;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.order.infrastructure.readExcel.ReadResult;
import com.rk.WMS.order.infrastructure.readExcel.RowError;
import com.rk.WMS.order.repository.ErrorFileImportRepository;
import com.rk.WMS.order.repository.OrderRepository;
import com.rk.WMS.order.service.OrderCodeService;
import com.rk.WMS.order.service.OrderImportService;
import com.rk.WMS.order.service.OrderService;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderImportServiceImpl implements OrderImportService {
  private static final String TEMPLATE_PATH = "template/importOrder/INB_ImportData.xlsx";
  private static final String FILE_NAME = "INB_ImportData.xlsx";

  private static final int START_ROW_DATA = 5;

  private static final int NO_COL = 0;
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
  private static final int ERROR_COL = 11;

  private final Validator validator;
  private final OrderMapper orderMapper;
  private final OrderRepository orderRepository;
  private final OrderService orderService;
  private final ErrorFileImportRepository errorFileImportRepository;
  private final CurrentUserProvider currentUserProvider;

  @Value("${file.storage-path}")
  private String storagePath;

  /**
   * @return trả về file template từ đường dẫn cố định
   */
  @Override
  public ResponseEntity<Resource> downloadTemplate() {
    ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);

    if (!resource.exists()) {
      throw new AppException(ErrorCode.TEMPLATE_NOT_FOUND);
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
  public ResponseEntity<Resource> downloadErrorFile(Long id) {
    ErrorFileImport errorFile = errorFileImportRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ERROR_FILE_NOT_FOUND));
    String path = errorFile.getPath();
    Resource resource = new FileSystemResource(path);
    if (!resource.exists()) {
      throw new AppException(ErrorCode.ERROR_FILE_NOT_FOUND);
    }
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ))
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + "INB_Import_Error_" + errorFile.getErrorFileId() + ".xlsx" + "\"")
        .body(resource);
  }

  /**
   * import nhiều đơn hàng từ file excel.
   * Luồng xử lí:
   * 1. đọc data
   * 2. nếu không lỗi: sinh mã đơn & lưu đơn hàng hàng loạt
   * 3. nếu lỗi: tạo file excel chứa các lỗi và lưu vào db, trả về id file lỗi
   *
   * @param file: file excel từ người dùng
   * @return
   * @throws IOException
   */
  @Transactional
  @Override
  public OrderImportResponse importExcel(MultipartFile file) throws IOException {
    //check format
    if (!EXCEL_FILE_FORMAT.equals(file.getContentType())) {
      throw new AppException(ErrorCode.FILE_FORMAT_INVALID);
    }
    //đọc file excel, nếu trả null tức là excel rỗng
    ReadResult result = readFile(file);
    if (result == null) {
      throw new AppException(ErrorCode.EMPTY_FILE);
    }
    List<CreateOrderRequest> createOrderRequestList = result.getValid();
    List<RowError> errors = result.getErrors();

    //nếu có dòng lỗi
    if (!errors.isEmpty()) {
      //tạo file excel chứa các lỗi
      byte[] workbookBytes = buildErrorWorkbook(errors);

      String fileName = "order-import-error-" + System.currentTimeMillis() + ".xlsx";
      Path directory = Paths.get(storagePath);
      Files.createDirectories(directory);

      //write file
      Path filePath = directory.resolve(fileName);
      Files.write(filePath, workbookBytes);

      // lưu DB
      ErrorFileImport errorFile = new ErrorFileImport();
      errorFile.setPath(filePath.toString());
      errorFile.setCreatedBy(currentUserProvider.getUserId());

      ErrorFileImport saved = errorFileImportRepository.save(errorFile);

      log.info("Created error file id={} with {} errors", saved.getErrorFileId(), errors.size());
      //trả id file lỗi cho người dùng
      return new OrderImportResponse(saved.getErrorFileId(), errors.size(), null);
    }

    //create order
    int total = orderService.createOrders(createOrderRequestList);
    log.info("Import {} orders successfully", total);


    return new OrderImportResponse(null, null, total);
  }

  /**
   * đọc file excel
   * Luồng xử lí:
   * 1. đọc data từng cell trong row rồi ghi vào dto
   * 2. validate dto
   * 3. ghi lại lỗi(nếu có)
   * 4. trả về kết quả
   * @param file: file excel từ người dùng
   * @return list dto hợp lệ và list lỗi
   * @throws IOException
   */
  private ReadResult readFile(MultipartFile file) throws IOException {
    List<CreateOrderRequest> resultValid = new ArrayList<>(); //list các row hợp lệ
    List<RowError> resultError = new ArrayList<>(); //list các row lỗi
    DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("vi-VN"));

    //tạo đối tượng để đọc file
    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = workbook.getSheet(SHEET_NAME);
      if (sheet == null) {
        throw new AppException(ErrorCode.SHEET_NOT_FOUND);
      }

      //đọc từng row
      for (int i = START_ROW_DATA; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) continue;

        //data của từng cột được lưu vào DTO
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

        //validate row qua DTO
        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(req);

        //nếu validate có lỗi, ghi lỗi đó vào resultError
        if (!violations.isEmpty()) {
          String errorMessage = violations.stream()
              .map(v -> v.getPropertyPath() + ": " + v.getMessage())
              .collect(Collectors.joining("; "));
          int excelRowNumber = i + 1;
          resultError.add(new RowError(excelRowNumber, errorMessage, req));
        } else {
          resultValid.add(req);
        }
      }

      return new ReadResult(resultValid, resultError);
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

  /**
   * tạo file excel chứa các lỗi
   * @param errors: list chứa các error
   * @return byte[]: file excel chứa các lỗi
   * @throws IOException
   */
  private byte[] buildErrorWorkbook(List<RowError> errors) throws IOException {
    try (Workbook wb = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = wb.createSheet(ERROR_SHEET_NAME);

      //tạo Header
      int rowError = 0;
      Row header = sheet.createRow(rowError++);
      header.createCell(NO_COL).setCellValue("STT");
      header.createCell(SUPPLIER_NAME_COL).setCellValue("Tên NCC");
      header.createCell(SUPPLIER_ADDRESS_COL).setCellValue("Địa chỉ NCC");
      header.createCell(SUPPLIER_PHONE_COL).setCellValue("SĐT NCC");
      header.createCell(SUPPLIER_EMAIL_COL).setCellValue("Email NCC");
      header.createCell(RECEIVER_NAME_COL).setCellValue("Tên người nhận");
      header.createCell(RECEIVER_ADDRESS_COL).setCellValue("Địa chỉ người nhận");
      header.createCell(RECEIVER_PHONE_COL).setCellValue("SĐT người nhận");
      header.createCell(RECEIVER_EMAIL_COL).setCellValue("Email người nhận");
      header.createCell(RECEIVER_LAT_COL).setCellValue("Vĩ độ người nhận");
      header.createCell(RECEIVER_LON_COL).setCellValue("Kinh độ người nhận");
      header.createCell(ERROR_COL).setCellValue("Lỗi");

      //ghi các dòng lỗi
      for (RowError e : errors) {
        Row row = sheet.createRow(rowError++);
        CreateOrderRequest req = e.getReq();
        row.createCell(NO_COL).setCellValue(e.getRowNumber() - START_ROW_DATA);

        row.createCell(SUPPLIER_NAME_COL).setCellValue(nullToEmpty(req.getSupplierName()));
        row.createCell(SUPPLIER_ADDRESS_COL).setCellValue(nullToEmpty(req.getSupplierAddress()));
        row.createCell(SUPPLIER_PHONE_COL).setCellValue(nullToEmpty(req.getSupplierPhone()));
        row.createCell(SUPPLIER_EMAIL_COL).setCellValue(nullToEmpty(req.getSupplierEmail()));

        row.createCell(RECEIVER_NAME_COL).setCellValue(nullToEmpty(req.getReceiverName()));
        row.createCell(RECEIVER_ADDRESS_COL).setCellValue(nullToEmpty(req.getReceiverAddress()));
        row.createCell(RECEIVER_PHONE_COL).setCellValue(nullToEmpty(req.getReceiverPhone()));
        row.createCell(RECEIVER_EMAIL_COL).setCellValue(nullToEmpty(req.getReceiverEmail()));

        if (req.getReceiverLat() != null)
          row.createCell(RECEIVER_LAT_COL).setCellValue(req.getReceiverLat());
        if (req.getReceiverLon() != null)
          row.createCell(RECEIVER_LON_COL).setCellValue(req.getReceiverLon());

        row.createCell(ERROR_COL).setCellValue(nullToEmpty(e.getErrorMessage()));
      }

      //sizing lại cột
      for (int i = 0; i <= ERROR_COL; i++) {
        sheet.autoSizeColumn(i);
      }

      wb.write(out);
      return out.toByteArray();
    }
  }

  /**
   * trả về empty string nếu như input là null
   */
  private String nullToEmpty(String s) {
    return s == null ? "" : s;
  }
}
