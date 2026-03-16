package com.rk.WMS.order.service;

import java.util.List;

public interface OrderLabelService {
  byte[] exportLabels(List<Integer> orderIds);

  String buildDownloadFileName();
}
