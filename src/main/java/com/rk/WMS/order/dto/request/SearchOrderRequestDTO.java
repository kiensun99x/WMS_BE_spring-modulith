package com.rk.WMS.order.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SearchOrderRequestDTO {
  String orderCode;
  String supplierPhone;
  String receiverPhone;
  Integer statusCode;
  String warehouseCode;
}
