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

//  @Value("${app.frontend.base-url:}")
  private String frontendBaseUrl = "http://localhost:8080";

  @Override
  public byte[] exportLabels(List<String> orderCodes) {
    if (orderCodes == null || orderCodes.isEmpty()) {
      return new byte[0];
    }

    // sanitize input (trim, distinct, giữ thứ tự)
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

    List<String> codes = new ArrayList<>(normalized);
    List<Order> orders = orderRepository.findByCodeIn(codes);

    // check tồn tại đầy đủ — thiếu cái nào cũng báo SYSS-1100
    if (orders.size() != codes.size()) {
      throw new AppException(ErrorCode.ORDER_NOT_FOUND); // SYSS-1100
    }

    // map theo code để sort đúng thứ tự user gửi lên
    Map<String, Order> byCode = new HashMap<>(orders.size());
    for (Order o : orders) {
      byCode.put(o.getCode(), o);
    }

    List<Order> sorted = new ArrayList<>(codes.size());
    for (String code : codes) {
      Order o = byCode.get(code);
      if (o == null) {
        throw new AppException(ErrorCode.ORDER_NOT_FOUND);
      }
      sorted.add(o);
    }

    LabelExcelGenerator generator = new LabelExcelGenerator();
    try (XSSFWorkbook wb = generator.generate(sorted, frontendBaseUrl);
        ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

      wb.write(baos);
      return baos.toByteArray();

    } catch (Exception e) {
      throw new IllegalStateException("Xuất nhãn thất bại", e);
    }
  }

  public String buildDownloadFileName() {
    return LabelExcelGenerator.buildFileName(LocalDate.now());
  }
}