package com.rk.WMS.auth.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {

    private Integer userId;
    private String username;
    private Integer warehouseId;
    private boolean isValid;
    private String message;
    private boolean authenticated;
    private List<String> authorities;
}