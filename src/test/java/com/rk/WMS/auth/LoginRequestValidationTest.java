package com.rk.WMS.auth;

import com.rk.WMS.auth.dto.request.LoginRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mục đích: Kiểm tra các annotation validation trên DTO
 */
class LoginRequestValidationTest {

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
     * Test case: DTO hợp lệ với tất cả các field
     * Expected: Không có constraint violation nào
     */
    @Test
    @DisplayName("Validation - DTO hợp lệ")
    void validLoginRequest() {
        // Given
        LoginRequest request = new LoginRequest(
                "validUser",
                "validPassword",
                1
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    /**
     * Test case: Username để trống (empty string)
     * Expected: 1 violation với message "Username không được để trống"
     */
    @Test
    @DisplayName("Validation - Username trống")
    void blankUsername() {
        // Given
        LoginRequest request = new LoginRequest(
                "",
                "password",
                1
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("Username không được để trống", violations.iterator().next().getMessage());
    }

    /**
     * Test case: Username là null
     * Expected: 1 violation với message "Username không được để trống"
     */
    @Test
    @DisplayName("Validation - Username null")
    void nullUsername() {
        // Given
        LoginRequest request = new LoginRequest(
                null,
                "password",
                1
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("Username không được để trống", violations.iterator().next().getMessage());
    }

    /**
     * Test case: Password để trống (empty string)
     * Expected: 1 violation với message "Password không được để trống"
     */
    @Test
    @DisplayName("Validation - Password trống")
    void blankPassword() {
        // Given
        LoginRequest request = new LoginRequest(
                "username",
                "",
                1
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("Password không được để trống", violations.iterator().next().getMessage());
    }

    /**
     * Test case: Password là null
     * Expected: 1 violation với message "Password không được để trống"
     */
    @Test
    @DisplayName("Validation - Password null")
    void nullPassword() {
        // Given
        LoginRequest request = new LoginRequest(
                "username",
                null,
                1
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("Password không được để trống", violations.iterator().next().getMessage());
    }

    /**
     * Test case: WarehouseId là null
     * Expected: 1 violation với message "WarehouseId không được để trống"
     */
    @Test
    @DisplayName("Validation - WarehouseId null")
    void nullWarehouseId() {
        // Given
        LoginRequest request = new LoginRequest(
                "username",
                "password",
                null
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("WarehouseId không được để trống", violations.iterator().next().getMessage());
    }

    /**
     * Test case: WarehouseId bằng 0 (không thỏa mãn @Min)
     * Expected: 1 violation với message "WarehouseId phải lớn hơn 0"
     */
    @Test
    @DisplayName("Validation - WarehouseId bằng 0 (Min validation)")
    void warehouseIdZero() {
        // Given
        LoginRequest request = new LoginRequest(
                "username",
                "password",
                0
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("WarehouseId phải lớn hơn 0", violations.iterator().next().getMessage());
    }

    /**
     * Test case: WarehouseId là số âm
     * Expected: 1 violation với message "WarehouseId phải lớn hơn 0"
     */
    @Test
    @DisplayName("Validation - WarehouseId là số âm")
    void warehouseIdNegative() {
        // Given
        LoginRequest request = new LoginRequest(
                "username",
                "password",
                -1
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("WarehouseId phải lớn hơn 0", violations.iterator().next().getMessage());
    }

    /**
     * Test case: Tất cả các field đều không hợp lệ
     * Expected: 3 violations (username, password, warehouseId)
     */
    @Test
    @DisplayName("Validation - Tất cả fields invalid")
    void allFieldsInvalid() {
        // Given
        LoginRequest request = new LoginRequest(
                "",
                "",
                null
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertEquals(3, violations.size());
    }
}