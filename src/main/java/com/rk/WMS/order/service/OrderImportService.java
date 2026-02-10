package com.rk.WMS.order.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface OrderImportService {
  ResponseEntity<Resource> downloadTemplate();
}
