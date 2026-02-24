package com.rk.WMS.order.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateOrderRequest {
  @NotBlank(message = "Tên nhà cung cấp không được để trống")
  @Size(max = 255, message = "Tên nhà cung cấp quá dài")
  private String supplierName;

  @NotBlank(message = "Địa chỉ nhà cung cấp không được để trống")
  @Size(max = 500, message = "Địa chỉ nhà cung cấp quá dài")
  private String supplierAddress;

  @NotBlank(message = "Số điện thoại nhà cung cấp không được để trống")
  @Pattern(regexp = "^(0|84)[0-9]{9}$", message = "Định dạng số điện thoại nhà cung cấp không hợp lệ")
  private String supplierPhone;

  @NotBlank(message = "Email nhà cung cấp không được để trống")
  @Email(message = "Định dạng email nhà cung cấp không hợp lệ")
  private String supplierEmail;

  @NotBlank(message = "Tên người nhận không được để trống")
  @Size(max = 255, message = "Tên người nhận quá dài")
  private String receiverName;

  @NotBlank(message = "Địa chỉ người nhận không được để trống")
  @Size(max = 500, message = "Địa chỉ người nhận quá dài")
  private String receiverAddress;

  @NotBlank(message = "Số điện thoại người nhận không được để trống")
  @Pattern(regexp = "^(0|84)[0-9]{9}$", message = "Định dạng số điện thoại người nhận không hợp lệ")
  private String receiverPhone;

  @NotBlank(message = "Email người nhận không được để trống")
  @Email(message = "Định dạng email người nhận không hợp lệ")
  private String receiverEmail;

  @NotNull(message = "Vĩ độ người nhận là bắt buộc")
  @DecimalMin(value = "-90.0", message = "Vĩ độ người nhận phải >= -90")
  @DecimalMax(value = "90.0", message = "Vĩ độ người nhận phải <= 90")
  private Double receiverLat;

  @NotNull(message = "Kinh độ người nhận là bắt buộc")
  @DecimalMin(value = "-180.0", message = "Kinh độ người nhận phải >= -180")
  @DecimalMax(value = "180.0", message = "Kinh độ người nhận phải <= 180")
  private Double receiverLon;
}