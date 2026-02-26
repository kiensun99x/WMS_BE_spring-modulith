package com.rk.WMS.batch.service;


import com.rk.WMS.batch.service.Impl.DistanceCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mục đích: Kiểm tra tính toán khoảng cách giữa 2 tọa độ địa lý
 * Sử dụng công thức Haversine
 */
class DistanceCalculatorTest {

    private DistanceCalculator distanceCalculator;

    @BeforeEach
    void setUp() {
        distanceCalculator = new DistanceCalculator();
    }

    /**
     * Test case: Tính khoảng cách giữa 2 điểm giống nhau
     * Expected: Khoảng cách = 0 km
     */
    @Test
    @DisplayName("distanceKm - Cùng tọa độ")
    void distanceKm_SameCoordinates() {
        // Given
        BigDecimal lat = BigDecimal.valueOf(10.762622);
        BigDecimal lon = BigDecimal.valueOf(106.660172);

        // When
        double distance = distanceCalculator.distanceKm(lat, lon, lat, lon);

        // Then
        assertEquals(0.0, distance, 0.0001);
    }





    /**
     * Test case: Tính khoảng cách với tọa độ âm (kinh độ Tây)
     */
    @Test
    @DisplayName("distanceKm - Kinh độ âm")
    void distanceKm_NegativeLongitude() {
        // Given
        BigDecimal lat1 = BigDecimal.valueOf(40.7128);
        BigDecimal lon1 = BigDecimal.valueOf(-74.0060); // New York
        BigDecimal lat2 = BigDecimal.valueOf(51.5074);
        BigDecimal lon2 = BigDecimal.valueOf(-0.1278);  // London

        // When
        double distance = distanceCalculator.distanceKm(lat1, lon1, lat2, lon2);

        // Then - New York đến London ~ 5570 km
        assertTrue(distance > 5500 && distance < 5600);
    }

    /**
     * Test case: Tính khoảng cách với điểm trên xích đạo
     */
    @Test
    @DisplayName("distanceKm - Điểm trên xích đạo")
    void distanceKm_EquatorPoints() {
        // Given
        BigDecimal lat1 = BigDecimal.ZERO; // Xích đạo
        BigDecimal lon1 = BigDecimal.ZERO; // Kinh tuyến gốc
        BigDecimal lat2 = BigDecimal.ZERO;
        BigDecimal lon2 = BigDecimal.valueOf(90); // 90 độ kinh Đông

        // When
        double distance = distanceCalculator.distanceKm(lat1, lon1, lat2, lon2);

        // Then - Trên xích đạo, 90 độ ~ 10,000 km (1/4 chu vi Trái Đất)
        assertTrue(distance > 10000 && distance < 10020);
    }

    /**
     * Test case: Tính khoảng cách với tọa độ null
     * Expected: Throw NullPointerException
     */
    @Test
    @DisplayName("distanceKm - Tọa độ null")
    void distanceKm_NullCoordinates() {
        // When & Then
        assertThrows(NullPointerException.class, () ->
                distanceCalculator.distanceKm(null, null, null, null));
    }
}
