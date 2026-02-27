package com.rk.WMS.order.infrastructure;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.rk.WMS.common.constants.DateTimePattern;
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
  private static final int TEMPLATE_SHEET_INDEX = 0;
  private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern(DateTimePattern.FILE_DATE);

  // cell hiển thị mã đơn
  private static final int ORDER_CODE_ROW = 1; // 0-based
  private static final int ORDER_CODE_COL = 8;

  // Vị trí chèn barcode (anchor theo cell)
  private static final int BARCODE_COL1 = 2;
  private static final int BARCODE_ROW1 = 3;
  private static final int BARCODE_COL2 = 8;
  private static final int BARCODE_ROW2 = 7;

  // Vị trí chèn QR
  private static final int QR_COL1 = 6;
  private static final int QR_ROW1 = 9;
  private static final int QR_COL2 = 9;
  private static final int QR_ROW2 = 18;

  //vị trí chèn data
  private static final int DATA_COL = 1;

  private static final int SUPPLIER_NAME_ROW = 9;
  private static final int SUPPLIER_PHONE_ROW = 10;
  private static final int SUPLIER_ADDRESS_ROW = 11;

  private static final int RECEIVER_NAME_ROW = 15;
  private static final int RECEIVER_PHONE_ROW = 16;
  private static final int RECEIVER_ADDRESS_ROW = 17;
  // ============================================================

  /**
   * tạo tên file: Labels_yyyyMMdd.xlsx
   * @param date
   * @return
   */
  public static String buildFileName(LocalDate date) {
    return "Labels_" + FILE_DATE.format(date) + ".xlsx";
  }

  /**
   * tạo file excel chứa các sheet là các label ứng với mỗi đơn hàng
   *
   * luồng hoạt động:
   * +) Kiểm tra null/empty
   * +) Load template workbook
   * +) Lặp qua từng đơn hàng để tạo từng label ứng với mỗi sheet:
   *  - Chuẩn hóa sheet name
   *  - Clone template sheet config, fill data, barcode, qrcode
   *  - Apply A5 print và margin cho sheet label
   *  - Insert sheet vào workbook
   * +) Return workbook
   *
   * @param orders: danh sách đơn hàng
   * @param frontendBaseUrl: url của frontend(để tạo qrcode dẫn tới trang Order Detail)
   * @return
   */
  public XSSFWorkbook generate(List<Order> orders, String frontendBaseUrl) {
    if (orders == null || orders.isEmpty()) {
      return new XSSFWorkbook();
    }

    //lấy file template
    XSSFWorkbook workbook = loadTemplateWorkbook();

    //lặp qua từng đơn hàng để tạo từng label ứng với mỗi sheet
    for (Order order : orders) {
      //chuẩn hóa sheet name(tránh lỗi khi tạo tên sheet)
      String orderCode = order.getCode();
      String safeSheetName = validateSheetName(orderCode);

      //clone template sheet config, fill data, barcode, qrcode
      Sheet sheet = workbook.cloneSheet(TEMPLATE_SHEET_INDEX);
      workbook.setSheetName(workbook.getSheetIndex(sheet), safeSheetName);

      applyA5PrintAndMargins(sheet);

      fillOrderData(sheet, order);
      insertBarcode(sheet, workbook, orderCode);
      insertQr(sheet, workbook, buildOrderDetailUrl(frontendBaseUrl, order));
    }

    // Xóa sheet template gốc để file chỉ còn sheet nhãn
    workbook.removeSheetAt(TEMPLATE_SHEET_INDEX);

    return workbook;
  }

  /**
   * load file template
   * @return file excel chứa sheet label mẫu để fill data
   */
  private XSSFWorkbook loadTemplateWorkbook() {
    try (InputStream is = new ClassPathResource(TEMPLATE_PATH).getInputStream()) {
      return new XSSFWorkbook(is);
    } catch (IOException e) {
      throw new IllegalStateException("Không đọc được template: " + TEMPLATE_PATH, e);
    }
  }

  /**
   * fill dữ liệu đơn hàng vào sheet label
   * @param sheet
   * @param order
   */
  private void fillOrderData(Sheet sheet, Order order) {
    setCellValue(sheet, ORDER_CODE_ROW, ORDER_CODE_COL, order.getCode());

    setCellValue(sheet, SUPPLIER_NAME_ROW, DATA_COL, order.getSupplierName());
    setCellValue(sheet, SUPLIER_ADDRESS_ROW, DATA_COL, order.getSupplierAddress());
    setCellValue(sheet, SUPPLIER_PHONE_ROW, DATA_COL, order.getSupplierPhone());

    setCellValue(sheet, RECEIVER_NAME_ROW, DATA_COL, order.getReceiverName());
    setCellValue(sheet, RECEIVER_ADDRESS_ROW, DATA_COL, order.getReceiverAddress());
    setCellValue(sheet, RECEIVER_PHONE_ROW, DATA_COL, order.getReceiverPhone());
  }

  /**
   * generate barcode và gán vào sheet label
   * @param sheet
   * @param workbook
   * @param orderCode
   */
  private void insertBarcode(Sheet sheet, XSSFWorkbook workbook, String orderCode) {
    try {
      byte[] png = createCode128Png(orderCode, 800, 200);

      int pictureIdx = workbook.addPicture(png, Workbook.PICTURE_TYPE_PNG);
      Drawing<?> drawing = sheet.createDrawingPatriarch();
      CreationHelper helper = workbook.getCreationHelper();

      ClientAnchor anchor = helper.createClientAnchor();
      anchor.setCol1(BARCODE_COL1);
      anchor.setRow1(BARCODE_ROW1);
      anchor.setCol2(BARCODE_COL2);
      anchor.setRow2(BARCODE_ROW2);
      anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

      drawing.createPicture(anchor, pictureIdx);
    } catch (Exception e) {
      throw new IllegalStateException("Tạo barcode thất bại cho orderCode=" + orderCode, e);
    }
  }

  /**
   * generate QR và gán vào sheet label
   *
   * QR code sẽ là đường dẫn tới trang Order Detail theo id
   *
   * @param sheet
   * @param workbook
   * @param url
   */
  private void insertQr(Sheet sheet, XSSFWorkbook workbook, String url) {
    try {
      byte[] png = createQrPng(url, 400, 400);

      int pictureIdx = workbook.addPicture(png, Workbook.PICTURE_TYPE_PNG);
      Drawing<?> drawing = sheet.createDrawingPatriarch();
      CreationHelper helper = workbook.getCreationHelper();

      ClientAnchor anchor = helper.createClientAnchor();
      anchor.setCol1(QR_COL1);
      anchor.setRow1(QR_ROW1);
      anchor.setCol2(QR_COL2);
      anchor.setRow2(QR_ROW2);
      anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

      drawing.createPicture(anchor, pictureIdx);
    } catch (Exception e) {
      throw new IllegalStateException("Tạo QR thất bại cho url=" + url, e);
    }
  }

  /**
   * build url dẫn tới trang Order Detail
   * @param frontendBaseUrl
   * @param order
   * @return
   */
  private String buildOrderDetailUrl(String frontendBaseUrl, Order order) {
    String base = (frontendBaseUrl == null) ? "" : frontendBaseUrl.trim();
    //validate bỏ dấu /
    if (base.endsWith("/")) base = base.substring(0, base.length() - 1);

    return base + "/orders/" + order.getId();
  }

  /**
   * apply A5 print và margin cho sheet label
   * @param sheet
   */
  private void applyA5PrintAndMargins(Sheet sheet) {
    PrintSetup ps = sheet.getPrintSetup();
    ps.setPaperSize(PrintSetup.A5_PAPERSIZE);
    ps.setLandscape(true);

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

  /**
   * get hoặc create row nếu chưa có row
   * @param sheet
   * @param rowIndex
   * @return
   */
  private static Row getOrCreateRow(Sheet sheet, int rowIndex) {
    Row row = sheet.getRow(rowIndex);
    return row != null ? row : sheet.createRow(rowIndex);
  }

  /**
   * get hoặc create cell nếu chưa có cell
   * @param row
   * @param colIndex
   * @return
   */
  private static Cell getOrCreateCell(Row row, int colIndex) {
    Cell cell = row.getCell(colIndex);
    return cell != null ? cell : row.createCell(colIndex);
  }

  /**
   * helper method set giá trị cho cell
   * @param sheet
   * @param rowIndex
   * @param colIndex
   * @param value
   */
  private void setCellValue(Sheet sheet, int rowIndex, int colIndex, String value) {
    Row row = getOrCreateRow(sheet, rowIndex);
    Cell cell = getOrCreateCell(row, colIndex);
    cell.setCellValue(value != null ? value : "");
  }

  /**
   * chuẩn hóa sheet name(tránh lỗi khi tạo tên sheet)
   *
   * +) Kiểm tra null/rỗng
   * +) Cắt độ dài
   * +) Replace ký tự ko hợp lệ
   *
   * @param input
   * @return
   */
  private static String validateSheetName(String input) {
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

  /**
   * tạo png barcode
   * @param content: nội dung QR code
   * @param width
   * @param height
   * @return
   * @throws WriterException
   * @throws IOException
   */
  private static byte[] createQrPng(String content, int width, int height) throws WriterException, IOException {
    QRCodeWriter writer = new QRCodeWriter();
    BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
    BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
    return bufferedImageToPng(image);
  }

  /**
   * tạo png barcode
   * @param content: nội dung barcode
   * @param width
   * @param height
   * @return
   * @throws WriterException
   * @throws IOException
   */
  private static byte[] createCode128Png(String content, int width, int height) throws WriterException, IOException {
    Code128Writer writer = new Code128Writer();
    BitMatrix matrix = writer.encode(content, BarcodeFormat.CODE_128, width, height);
    BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
    return bufferedImageToPng(image);
  }

  /**
   * convert BufferedImage to png byte array
   * @param image
   * @return
   * @throws IOException
   */
  private static byte[] bufferedImageToPng(BufferedImage image) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ImageIO.write(image, "png", baos);
      return baos.toByteArray();
    }
  }
}