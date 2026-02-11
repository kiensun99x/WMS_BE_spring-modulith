package com.rk.WMS.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    NEW(0, "Created"),
    STORED(1, "Stored"),
    DELIVERED(2, "Delivered"),
    FAILED(3, "Failed"),
    RETURNED(4, "Returned");

    private final int code;
    private final String description;

    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid order status code: " + code);
    }
}

