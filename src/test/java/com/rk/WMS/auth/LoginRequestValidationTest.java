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

class LoginRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


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