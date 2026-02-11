package com.rk.WMS.auth.event;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserLoginSuccessEvent extends ApplicationEvent {

    private final Long userId;
    private final Integer warehouseId;
    private final String username;

    public UserLoginSuccessEvent(
            Object source,
            Long userId,
            Integer warehouseId,
            String username) {

        super(source);
        this.userId = userId;
        this.warehouseId = warehouseId;
        this.username = username;
    }
}
