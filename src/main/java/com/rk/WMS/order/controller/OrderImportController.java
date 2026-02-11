package com.rk.WMS.order.controller;

import com.rk.WMS.auth.dto.response.LoginResponse;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.common.response.ApiResponse;
import com.rk.WMS.order.service.OrderImportService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j(topic = "ORDER-IMPORT-CONTROLLER")
@RestController
@RequestMapping("/orders/import")
@RequiredArgsConstructor
public class OrderImportController {

  private final OrderImportService orderImportService;

  @GetMapping("/template")
  public ResponseEntity<Resource> downloadTemplate() {
    log.info("[ORDER-IMPORT][API][REQUEST]: DOWNLOAD-TEMPLATE");
    return orderImportService.downloadTemplate();
  }

  @PostMapping("/")
  public ApiResponse<?> importOrders(@RequestParam("file") MultipartFile file) throws IOException {
    orderImportService.importExcel(file);
    log.info("[ORDER-IMPORT][API][REQUEST]: IMPORT-ORDERS");
    return ApiResponse.builder()
        .code(ErrorCode.SUCCESS.getCode())
        .message("Import orders success")
        .result(null) //sẽ phải thay bằng list chứa ordercode tạo ra
        .build();
  }
}
