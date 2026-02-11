package com.rk.WMS.order.service;

import com.rk.WMS.order.dto.response.OrderImportResponse;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface OrderImportService {
  ResponseEntity<Resource> downloadTemplate();

  OrderImportResponse importExcel(MultipartFile file) throws IOException;
}
