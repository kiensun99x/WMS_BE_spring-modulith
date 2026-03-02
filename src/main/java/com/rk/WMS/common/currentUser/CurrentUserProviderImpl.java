package com.rk.WMS.common.currentUser;


import com.rk.WMS.config.CustomUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProviderImpl implements CurrentUserProvider {

    public Long getUserId() {
        return getPrincipal().getUserId();
    }

    public Long getWarehouseId() {
        return getPrincipal().getWarehouseId();
    }

    private CustomUserPrincipal getPrincipal() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof CustomUserPrincipal principal)) {
            throw new RuntimeException("Unauthenticated");
        }

        return principal;
    }
}
