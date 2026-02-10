package com.rk.WMS.order.controller;

import com.rk.WMS.order.service.OrderImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j(topic = "ORDER-IMPORT-CONTROLLER")
@RestController
@RequestMapping("/orders/import")
@RequiredArgsConstructor
public class OrderImportController {

  private final OrderImportService templateService;

  @GetMapping("/template")
  public ResponseEntity<Resource> downloadTemplate() {
    log.info("[ORDER-IMPORT][API][REQUEST]: DOWNLOAD-TEMPLATE");
    return templateService.downloadTemplate();
  }
}
