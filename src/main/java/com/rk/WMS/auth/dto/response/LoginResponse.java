package com.rk.WMS.auth.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private Integer userId;
    private String username;
    private String fullName;
    private Integer warehouseId;

    private String accessToken;
    private boolean authenticated;
}

