package com.rk.WMS.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ===== COMMON =====
    SUCCESS("SYSS-0001", "Thành công", HttpStatus.OK),
    FAILED("SYSS-0002", "Thất bại", HttpStatus.BAD_REQUEST),

    STRING_EXCEED_MAX_LENGTH("SYSS-0003", "Chuỗi vượt quá số ký tự", HttpStatus.BAD_REQUEST),
    STRING_ONLY_NUMBER("SYSS-0004", "Chuỗi chỉ bao gồm số", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT("SYSS-0005", "Thông tin không đúng định dạng", HttpStatus.BAD_REQUEST),
    VALUE_LESS_THAN_MIN("SYSS-0006", "Số vượt quá giá trị tối thiểu", HttpStatus.BAD_REQUEST),
    VALUE_GREATER_THAN_MAX("SYSS-0007", "Số vượt quá giá trị tối đa", HttpStatus.BAD_REQUEST),

    MISSING_START_TIME("SYSS-0008", "Thiếu thông tin thời gian bắt đầu", HttpStatus.BAD_REQUEST),
    MISSING_END_TIME("SYSS-0009", "Thiếu thông tin thời gian kết thúc", HttpStatus.BAD_REQUEST),
    VALUE_EXCEED_LIMIT("SYSS-00010", "Giá trị vượt quá quy định <số><đơn vị tính>", HttpStatus.BAD_REQUEST),

    // ===== ORDER =====
    ORDER_NOT_FOUND("SYSS-1100", "Đơn hàng không tồn tại trong hệ thống", HttpStatus.NOT_FOUND),
    ORDER_NOT_CONFIRMED("SYSS-1101", "Đơn hàng không ở trạng thái xác nhận", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS("SYSS-1102", "Đơn hàng không ở trạng thái đơn mới", HttpStatus.BAD_REQUEST),

    // ===== ORDER IMPORT Excel =====
    ORDER_IMPORT_HAS_ERRORS("SYSS-1102", "File import có dòng lỗi", HttpStatus.UNPROCESSABLE_ENTITY),
    SHEET_NOT_FOUND("SYSS-1103", "Sheet 'Orders' không tồn tại trong file Excel", HttpStatus.BAD_REQUEST),
    EMPTY_FILE("SYSS-1104", "File import không có dữ liệu", HttpStatus.BAD_REQUEST),
    FILE_FORMAT_INVALID("SYSS-1105", "File import không đúng định dạng", HttpStatus.BAD_REQUEST),
    // ===== WAREHOUSE =====
    WAREHOUSE_NOT_FOUND("SYSS-1200", "Kho hàng không tồn tại trong hệ thống", HttpStatus.NOT_FOUND),

    // ===== AUTH / ACCOUNT =====
    ACCOUNT_NOT_FOUND("SYSS-2000", "Tên tài khoản không tồn tại trong hệ thống", HttpStatus.NOT_FOUND),
    INVALID_LOGIN_INFO("SYSS-2001", "Thông tin đăng nhập chưa chính xác", HttpStatus.UNAUTHORIZED),

    VALIDATION_ERROR("40001", "Validation error", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("50000", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),

    NO_AVAILABLE_WAREHOUSE("SYSS-3001", "Không có kho hợp lệ", HttpStatus.NOT_FOUND);


    private final String code;
    private final String message;
    private final HttpStatus statusCode;
}
