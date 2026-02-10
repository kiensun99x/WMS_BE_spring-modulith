package com.rk.WMS.order.service.impl;

import com.rk.WMS.order.service.OrderImportService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class OrderImportServiceImpl implements OrderImportService {
  private static final String TEMPLATE_PATH = "template/importOrder/INB_ImportData.xlsx";
  private static final String FILE_NAME = "INB_ImportData.xlsx";

  @Override
  public ResponseEntity<Resource> downloadTemplate() {
    ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);

    if (!resource.exists()) {
      throw new RuntimeException("Template file not found at classpath:" + TEMPLATE_PATH);
    }

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ))
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + FILE_NAME + "\"")
        .body(resource);
  }
}
