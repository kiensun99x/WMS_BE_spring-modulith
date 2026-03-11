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
import org.springframework.data.domain.Sort;
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
   * @param orderIds: danh sách order id
   * @return
   */
  @Override
  public byte[] exportLabels(List<Integer> orderIds) {
    if (orderIds == null || orderIds.isEmpty()) {
      return new byte[0];
    }

    LinkedHashSet<Integer> normalized = new LinkedHashSet<>();
    for (Integer orderId : orderIds) {
      if (orderId != null) {
        normalized.add(orderId);
      }
    }

    if (normalized.isEmpty()) {
      return new byte[0];
    }

    if (normalized.size() > 10) {
      // Request DTO đã chặn bằng @Size, đoạn này chỉ để “đỡ bị bypass”
      throw new AppException(ErrorCode.VALIDATION_ERROR);
    }

    List<Integer> ids = new ArrayList<>(normalized);
    List<Order> orders = orderRepository.findByIdInOrderByIdAsc(ids);

    // check tồn tại đầy đủ — thiếu cái nào cũng báo SYSS-1100
    if (orders.size() != ids.size()) {
      throw new AppException(ErrorCode.ORDER_NOT_FOUND);
    }

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