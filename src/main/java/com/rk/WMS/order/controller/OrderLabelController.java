package com.rk.WMS.order.controller;

import com.rk.WMS.common.constants.ExcelFilePattern;
import com.rk.WMS.order.dto.request.ExportLabelsRequest;
import com.rk.WMS.order.service.OrderLabelService;
import com.rk.WMS.order.service.impl.OrderLabelServiceImpl;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j(topic = "ORDER-LABEL-CONTROLLER")
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders/labels")
public class OrderLabelController {

  private final OrderLabelService orderLabelService;

  @PostMapping("/")
  public ResponseEntity<byte[]> exportLabels(@Valid @RequestBody ExportLabelsRequest request) {
    byte[] bytes = orderLabelService.exportLabels(request.getOrderIds());

    // Lấy tên file đúng format Labels_YYYYmmDD.xlsx
    String filename = orderLabelService.buildDownloadFileName();


    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(
        ExcelFilePattern.EXCEL_FILE_FORMAT
    ));
    headers.setContentDisposition(ContentDisposition.attachment()
        .filename(filename, StandardCharsets.UTF_8)
        .build()
    );
    log.info("Export label file with {} orders", request.getOrderIds().size());
    return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
  }
}