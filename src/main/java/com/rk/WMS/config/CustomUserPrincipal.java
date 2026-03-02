package com.rk.WMS.config;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@AllArgsConstructor
public class CustomUserPrincipal {

    private Long userId;
    private String username;
    private Long warehouseId;
    private Collection<? extends GrantedAuthority> authorities;
}
