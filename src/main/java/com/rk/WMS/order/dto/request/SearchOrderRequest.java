package com.rk.WMS.order.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SearchOrderRequest {
  String orderCode;
  String supplierPhone;
  String receiverPhone;
  Integer statusCode;
  String warehouseCode;
}
