package com.rk.WMS.order.controller;

import com.rk.WMS.order.dto.request.ExportLabelsRequest;
import com.rk.WMS.order.service.OrderLabelService;
import com.rk.WMS.order.service.impl.OrderLabelServiceImpl;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders/labels")
public class OrderLabelController {

  private final OrderLabelService orderLabelService;

  @PostMapping("/")
  public ResponseEntity<byte[]> exportLabels(@Valid @RequestBody ExportLabelsRequest request) {
    byte[] bytes = orderLabelService.exportLabels(request.getOrderCodes());

    // Lấy tên file đúng format Labels_YYYYmmDD.xlsx
    String filename = (orderLabelService instanceof OrderLabelServiceImpl impl)
        ? impl.buildDownloadFileName()
        : "Labels.xlsx";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    ));
    headers.setContentDisposition(ContentDisposition.attachment()
        .filename(filename, StandardCharsets.UTF_8)
        .build()
    );

    return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
  }
}