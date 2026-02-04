package com.rk.WMS.auth.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Password không được để trống")
    private String password;

    @NotNull(message = "WarehouseId không được để trống")
    @Min(value = 1, message = "WarehouseId phải lớn hơn 0")
    private Integer warehouseId;
}
