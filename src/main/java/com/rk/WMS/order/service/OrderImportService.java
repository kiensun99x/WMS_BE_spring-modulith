package com.rk.WMS.order.service;

import com.rk.WMS.order.dto.request.CreateOrderRequest;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface OrderImportService {
  ResponseEntity<Resource> downloadTemplate();

  void importExcel(MultipartFile file) throws IOException;
}
