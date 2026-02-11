package com.rk.WMS.common.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.rk.WMS.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j(topic = "GLOBAL-EXCEPTION")
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(
            AppException ex,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ex.getErrorCode();

        log.warn("Business error [{}]: {}",
                errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(buildError(errorCode, request, errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse(errorCode.getMessage());

        log.warn("Validation error: {}", message);

        return ResponseEntity
                .badRequest()
                .body(buildError(errorCode, request, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_FORMAT;
        String message = "Request body không hợp lệ hoặc thiếu thông tin";

        if (ex.getCause() instanceof InvalidFormatException) {
            message = "Định dạng dữ liệu không hợp lệ";
        }

        log.warn("Invalid request body: {}", ex.getMessage());

        return ResponseEntity
                .badRequest()
                .body(buildError(errorCode, request, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(
            Exception ex,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;

        log.error("Unhandled exception", ex);

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(buildError(errorCode, request, errorCode.getMessage()));
    }

    private ErrorResponse buildError(
            ErrorCode errorCode,
            HttpServletRequest request,
            String message
    ) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(errorCode.getCode())
                .path(request.getRequestURI())
                .error(errorCode.getStatusCode().getReasonPhrase())
                .message(message)
                .build();
    }
}
