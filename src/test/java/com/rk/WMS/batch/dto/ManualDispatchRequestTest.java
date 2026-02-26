package com.rk.WMS.batch.dto;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mục đích: Kiểm tra validation annotations trên DTO
 */
class ManualDispatchRequestTest {

    private Validator validator;

    /**
     * Setup trước mỗi test case
     * Khởi tạo validator từ Validation factory
     */
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Test case: Request hợp lệ
     * Expected: Không có constraint violation
     */
    @Test
    @DisplayName("Validation - Request hợp lệ")
    void validRequest() {
        // Given
        ManualDispatchRequest request = new ManualDispatchRequest();
        request.setOrderIds(Arrays.asList(1L, 2L, 3L));
        request.setWarehouseId(100L);

        // When
        Set<ConstraintViolation<ManualDispatchRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    /**
     * Test case: Request với orderIds rỗng
     * Expected: 1 violation với message "Danh sách orderIds không được để trống"
     */
    @Test
    @DisplayName("Validation - orderIds rỗng")
    void emptyOrderIds() {
        // Given
        ManualDispatchRequest request = new ManualDispatchRequest();
        request.setOrderIds(Collections.emptyList());
        request.setWarehouseId(100L);

        // When
        Set<ConstraintViolation<ManualDispatchRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("Danh sách orderIds không được để trống", violations.iterator().next().getMessage());
    }

    /**
     * Test case: Request với orderIds null
     * Expected: 1 violation với message "Danh sách orderIds không được để trống"
     */
    @Test
    @DisplayName("Validation - orderIds null")
    void nullOrderIds() {
        // Given
        ManualDispatchRequest request = new ManualDispatchRequest();
        request.setOrderIds(null);
        request.setWarehouseId(100L);

        // When
        Set<ConstraintViolation<ManualDispatchRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("Danh sách orderIds không được để trống", violations.iterator().next().getMessage());
    }

    /**
     * Test case: Request với warehouseId null
     * Expected: 1 violation với message "WarehouseId không được để trống"
     */
    @Test
    @DisplayName("Validation - warehouseId null")
    void nullWarehouseId() {
        // Given
        ManualDispatchRequest request = new ManualDispatchRequest();
        request.setOrderIds(Arrays.asList(1L, 2L, 3L));
        request.setWarehouseId(null);

        // When
        Set<ConstraintViolation<ManualDispatchRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("WarehouseId không được để trống", violations.iterator().next().getMessage());
    }

    /**
     * Test case: Tất cả fields đều invalid
     * Expected: 2 violations (orderIds và warehouseId)
     */
    @Test
    @DisplayName("Validation - Tất cả fields invalid")
    void allFieldsInvalid() {
        // Given
        ManualDispatchRequest request = new ManualDispatchRequest();
        request.setOrderIds(null);
        request.setWarehouseId(null);

        // When
        Set<ConstraintViolation<ManualDispatchRequest>> violations = validator.validate(request);

        // Then
        assertEquals(2, violations.size());
    }

    /**
     * Test case: Getter/Setter hoạt động đúng
     */
    @Test
    @DisplayName("Getter/Setter - Khởi tạo và lấy dữ liệu")
    void getterSetter() {
        // Given
        ManualDispatchRequest request = new ManualDispatchRequest();
        List<Long> orderIds = Arrays.asList(1L, 2L, 3L);
        Long warehouseId = 100L;

        // When
        request.setOrderIds(orderIds);
        request.setWarehouseId(warehouseId);

        // Then
        assertEquals(orderIds, request.getOrderIds());
        assertEquals(warehouseId, request.getWarehouseId());
    }
}
