package com.rk.WMS.order.infrastructure;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.rk.WMS.order.model.Order;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;

public class LabelExcelGenerator {

  private static final String TEMPLATE_PATH = "template/exportLabel/OB_DeliveryLabelTemplate.xlsx";
  private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

  // ======= TODO: chỉnh theo layout template thật của bạn =======
  // Ví dụ: cell hiển thị mã đơn
  private static final int ORDER_CODE_ROW = 1; // 0-based
  private static final int ORDER_CODE_COL = 7;

  // Vị trí chèn barcode (anchor theo cell)
  private static final int BARCODE_COL1 = 2;
  private static final int BARCODE_ROW1 = 13;
  private static final int BARCODE_COL2 = 7;
  private static final int BARCODE_ROW2 = 16;

  // Vị trí chèn QR
  private static final int QR_COL1 = 6;
  private static final int QR_ROW1 = 5;
  private static final int QR_COL2 = 8;
  private static final int QR_ROW2 = 9;

  //vị trí chèn data
  private static final int DATA_COL = 2;

  private static final int SUPPLIER_NAME_ROW = 4;
  private static final int SUPLIER_ADDRESS_ROW = 5;
  private static final int SUPPLIER_PHONE_ROW = 6;

  private static final int RECEIVER_NAME_ROW = 9;
  private static final int RECEIVER_ADDRESS_ROW = 10;
  private static final int RECEIVER_PHONE_ROW = 11;
  // ============================================================

  public static String buildFileName(LocalDate date) {
    return "Labels_" + FILE_DATE.format(date) + ".xlsx";
  }

  public XSSFWorkbook generate(List<Order> orders, String frontendBaseUrl) {
    if (orders == null || orders.isEmpty()) {
      return new XSSFWorkbook();
    }

    XSSFWorkbook workbook = loadTemplateWorkbook();
    int templateSheetIndex = 0;

    for (Order order : orders) {
      String orderCode = order.getCode();
      String safeSheetName = sanitizeSheetName(orderCode);

      Sheet sheet = workbook.cloneSheet(templateSheetIndex);
      int newIndex = workbook.getSheetIndex(sheet);
      workbook.setSheetName(newIndex, safeSheetName);

      applyA5PrintAndMargins(sheet);

      fillOrderData(sheet, order);
      insertBarcode(sheet, workbook, orderCode);
      insertQr(sheet, workbook, buildOrderDetailUrl(frontendBaseUrl, order));
    }

    // Xóa sheet template gốc để file chỉ còn sheet nhãn
    workbook.removeSheetAt(templateSheetIndex);

    return workbook;
  }

  private XSSFWorkbook loadTemplateWorkbook() {
    try (InputStream is = new ClassPathResource(TEMPLATE_PATH).getInputStream()) {
      return new XSSFWorkbook(is);
    } catch (IOException e) {
      throw new IllegalStateException("Không đọc được template: " + TEMPLATE_PATH, e);
    }
  }

  private void fillOrderData(Sheet sheet, Order order) {
    // TODO: mapping theo đúng template của bạn
    Row row = getOrCreateRow(sheet, ORDER_CODE_ROW);
    Cell cell = getOrCreateCell(row, ORDER_CODE_COL);
    cell.setCellValue(order.getCode());

    // ... TODO: fill thêm các thông tin khác (receiver, phone, address, ...)
    //supplier name
    Row supplierRow = getOrCreateRow(sheet, SUPPLIER_NAME_ROW);
    Cell supplierNameCell = getOrCreateCell(supplierRow, DATA_COL);
    supplierNameCell.setCellValue(order.getSupplierName());
    //supplier address
    Row supplierAddressRow = getOrCreateRow(sheet, SUPLIER_ADDRESS_ROW);
    Cell supplierAddressCell = getOrCreateCell(supplierAddressRow, DATA_COL);
    supplierAddressCell.setCellValue(order.getSupplierAddress());
    //supplier phone
    Row supplierPhoneRow = getOrCreateRow(sheet, SUPPLIER_PHONE_ROW);
    Cell supplierPhoneCell = getOrCreateCell(supplierPhoneRow, DATA_COL);
    supplierPhoneCell.setCellValue(order.getSupplierPhone());
    //receiver name
    Row receiverRow = getOrCreateRow(sheet, RECEIVER_NAME_ROW);
    Cell receiverNameCell = getOrCreateCell(receiverRow, DATA_COL);
    receiverNameCell.setCellValue(order.getReceiverName());
    //receiver address
    Row receiverAddressRow = getOrCreateRow(sheet, RECEIVER_ADDRESS_ROW);
    Cell receiverAddressCell = getOrCreateCell(receiverAddressRow, DATA_COL);
    receiverAddressCell.setCellValue(order.getReceiverAddress());
    //receiver phone
    Row receiverPhoneRow = getOrCreateRow(sheet, RECEIVER_PHONE_ROW);
    Cell receiverPhoneCell = getOrCreateCell(receiverPhoneRow, DATA_COL);
    receiverPhoneCell.setCellValue(order.getReceiverPhone());
  }

  private void insertBarcode(Sheet sheet, XSSFWorkbook workbook, String orderCode) {
    try {
      byte[] png = createCode128Png(orderCode, 520, 140);

      int pictureIdx = workbook.addPicture(png, Workbook.PICTURE_TYPE_PNG);
      Drawing<?> drawing = sheet.createDrawingPatriarch();
      CreationHelper helper = workbook.getCreationHelper();

      ClientAnchor anchor = helper.createClientAnchor();
      anchor.setCol1(BARCODE_COL1);
      anchor.setRow1(BARCODE_ROW1);
      anchor.setCol2(BARCODE_COL2);
      anchor.setRow2(BARCODE_ROW2);

      drawing.createPicture(anchor, pictureIdx);
    } catch (Exception e) {
      throw new IllegalStateException("Tạo barcode thất bại cho orderCode=" + orderCode, e);
    }
  }

  private void insertQr(Sheet sheet, XSSFWorkbook workbook, String url) {
    try {
      byte[] png = createQrPng(url, 220, 220);

      int pictureIdx = workbook.addPicture(png, Workbook.PICTURE_TYPE_PNG);
      Drawing<?> drawing = sheet.createDrawingPatriarch();
      CreationHelper helper = workbook.getCreationHelper();

      ClientAnchor anchor = helper.createClientAnchor();
      anchor.setCol1(QR_COL1);
      anchor.setRow1(QR_ROW1);
      anchor.setCol2(QR_COL2);
      anchor.setRow2(QR_ROW2);

      drawing.createPicture(anchor, pictureIdx);
    } catch (Exception e) {
      throw new IllegalStateException("Tạo QR thất bại cho url=" + url, e);
    }
  }

  private String buildOrderDetailUrl(String frontendBaseUrl, Order order) {
    String base = (frontendBaseUrl == null) ? "" : frontendBaseUrl.trim();
    if (base.endsWith("/")) base = base.substring(0, base.length() - 1);

    // TODO: đổi path đúng với FE của bạn
    // Option 1: theo mã đơn:
    return base + "/orders/" + order.getCode();
    // Option 2: theo id:
    // return base + "/orders/" + order.getId();
  }

  private void applyA5PrintAndMargins(Sheet sheet) {
    PrintSetup ps = sheet.getPrintSetup();
    ps.setPaperSize(PrintSetup.A5_PAPERSIZE);

    sheet.setMargin(Sheet.TopMargin, cmToInch(1.9));
    sheet.setMargin(Sheet.BottomMargin, cmToInch(1.9));
    sheet.setMargin(Sheet.LeftMargin, cmToInch(1.4));
    sheet.setMargin(Sheet.RightMargin, cmToInch(1.4));
    sheet.setMargin(Sheet.HeaderMargin, cmToInch(0.8));
    sheet.setMargin(Sheet.FooterMargin, cmToInch(0.8));
  }

  private static double cmToInch(double cm) {
    return cm * 0.3937007874d;
  }

  private static Row getOrCreateRow(Sheet sheet, int rowIndex) {
    Row row = sheet.getRow(rowIndex);
    return row != null ? row : sheet.createRow(rowIndex);
  }

  private static Cell getOrCreateCell(Row row, int colIndex) {
    Cell cell = row.getCell(colIndex);
    return cell != null ? cell : row.createCell(colIndex);
  }

  private static String sanitizeSheetName(String input) {
    if (input == null || input.isBlank()) {
      return "Sheet";
    }
    String s = input.replaceAll("[\\\\/?*\\[\\]:]", "_");
    s = s.trim();
    if (s.length() > 31) {
      s = s.substring(0, 31);
    }
    return s.isEmpty() ? "Sheet" : s;
  }

  private static byte[] createQrPng(String content, int width, int height) throws WriterException, IOException {
    QRCodeWriter writer = new QRCodeWriter();
    BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
    BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
    return bufferedImageToPng(image);
  }

  private static byte[] createCode128Png(String content, int width, int height) throws WriterException, IOException {
    Code128Writer writer = new Code128Writer();
    BitMatrix matrix = writer.encode(content, BarcodeFormat.CODE_128, width, height);
    BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
    return bufferedImageToPng(image);
  }

  private static byte[] bufferedImageToPng(BufferedImage image) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ImageIO.write(image, "png", baos);
      return baos.toByteArray();
    }
  }
}