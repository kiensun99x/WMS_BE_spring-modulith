package com.rk.WMS.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateOrderRequest {
  @NotBlank(message = "Supplier name is required")
  private String supplierName;
  @NotBlank(message = "Supplier address is required")
  private String supplierAddress;
  @NotBlank(message = "Supplier phone is required")
  private String supplierPhone;
  @NotBlank(message = "Supplier email is required")
  private String supplierEmail;

  @NotBlank(message = "Receiver name is required")
  private String receiverName;
  @NotBlank(message = "Receiver address is required")
  private String receiverAddress;
  @NotBlank(message = "Receiver phone is required")
  private String receiverPhone;
  @NotBlank(message = "Receiver email is required")
  private String receiverEmail;

  @NotNull(message = "Receiver latitude is required")
  private Double receiverLat;
  @NotNull(message = "Receiver longitude is required")
  private Double receiverLon;
}
