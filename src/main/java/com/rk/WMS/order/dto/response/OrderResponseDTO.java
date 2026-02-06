package com.rk.WMS.order.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderResponseDTO {

  private Integer id;
  private String code;

  private String status;
  private Integer statusCode;

  private Integer warehouseId;
  private String warehouseCode;
  private String warehouseName;

  private String supplierName;
  private String supplierAddress;
  private String supplierPhone;
  private String supplierEmail;

  private String receiverName;
  private String receiverPhone;
  private String receiverAddress;
  private String receiverEmail;

  private LocalDateTime createdAt;

  private Integer failedDeliveryCount;
}

