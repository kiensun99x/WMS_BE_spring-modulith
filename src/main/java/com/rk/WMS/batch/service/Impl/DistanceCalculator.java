package com.rk.WMS.batch.service.Impl;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DistanceCalculator {


    /**
     * Bán kính Trái Đất (km)
     */
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Tính khoảng cách (km) giữa hai tọa độ địa lý.
     *
     * @param lat1 vĩ độ điểm 1
     * @param lon1 kinh độ điểm 1
     * @param lat2 vĩ độ điểm 2
     * @param lon2 kinh độ điểm 2
     * @return khoảng cách tính bằng km
     */
    public double distanceKm(
            BigDecimal lat1, BigDecimal lon1,
            BigDecimal lat2, BigDecimal lon2
    ) {
        // Chuyển chênh lệch tọa độ sang radian
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        // Công thức Haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue()))
                * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        // Khoảng cách cuối cùng (km)
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}

