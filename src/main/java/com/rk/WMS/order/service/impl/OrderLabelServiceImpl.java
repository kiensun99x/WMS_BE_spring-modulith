package com.rk.WMS.order.service.impl;

import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.order.infrastructure.LabelExcelGenerator;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.order.repository.OrderRepository;
import com.rk.WMS.order.service.OrderLabelService;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderLabelServiceImpl implements OrderLabelService {

  private final OrderRepository orderRepository;

  @Value("${app.frontend.base-url:}")
  private String frontendBaseUrl = "http://localhost:8080";

  /**
   * tạo file excel chứa các label
   * luồng hoạt động:
   *    * +) Validate dữ liệu
   *    * +) Load template workbook
   *    * +) Lặp qua từng đơn hàng để tạo từng label ứng với mỗi sheet:
   *    *  - Chuẩn hóa sheet name
   *    *  - Clone template sheet config, fill data, barcode, qrcode
   *    *  - Apply A5 print và margin cho sheet label
   *    *  - Insert sheet vào workbook
   *    * +) Return workbook
   *
   * @param orderCodes: danh sách order code
   * @return
   */
  @Override
  public byte[] exportLabels(List<String> orderCodes) {
    if (orderCodes == null || orderCodes.isEmpty()) {
      return new byte[0];
    }

    // chuẩn hóa input, validate (trim, distinct, giữ thứ tự)
    LinkedHashSet<String> normalized = new LinkedHashSet<>();
    for (String c : orderCodes) {
      if (c != null && !c.trim().isEmpty()) {
        normalized.add(c.trim());
      }
    }

    if (normalized.isEmpty()) {
      return new byte[0];
    }

    if (normalized.size() > 10) {
      // Request DTO đã chặn bằng @Size, đoạn này chỉ để “đỡ bị bypass”
      throw new AppException(ErrorCode.VALIDATION_ERROR);
    }

    //lấy ra orderList theo code
    List<String> codes = new ArrayList<>(normalized);
    List<Order> orders = orderRepository.findByCodeInOrderByCodeAsc(codes);

    // check tồn tại đầy đủ — thiếu cái nào cũng báo SYSS-1100
    if (orders.size() != codes.size()) {
      throw new AppException(ErrorCode.ORDER_NOT_FOUND); // SYSS-1100
    }

    //tạo file
    LabelExcelGenerator generator = new LabelExcelGenerator();
    try (XSSFWorkbook wb = generator.generate(orders, frontendBaseUrl);
        ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

      wb.write(baos);
      return baos.toByteArray();

    } catch (Exception e) {
      throw new IllegalStateException("Xuất nhãn thất bại", e);
    }
  }

  /**
   * tạo tên file: Labels_yyyyMMdd.xlsx
   * @return
   */
  public String buildDownloadFileName() {
    return LabelExcelGenerator.buildFileName(LocalDate.now());
  }
}