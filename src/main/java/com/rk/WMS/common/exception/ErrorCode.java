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

    INVALID_ORDER_STATUS_FOR_DELIVERY("SYSS-1103", "Đơn hàng không ở trạng thái hợp lệ để giao hàng", HttpStatus.BAD_REQUEST),
    FAILURE_REASON_REQUIRED("SYSS-1104", "Thiếu lý do giao hàng thất bại", HttpStatus.BAD_REQUEST),
    FAILURE_REASON_NOT_FOUND("SYSS-1105", "Lý do giao hàng thất bại không tồn tại", HttpStatus.NOT_FOUND),

    // ===== ORDER IMPORT Excel =====
    FILE_FORMAT_INVALID("SYSS-1200", "File import không đúng định dạng", HttpStatus.BAD_REQUEST),
    SHEET_NOT_FOUND("SYSS-1201", "Sheet 'Orders' không tồn tại trong file Excel", HttpStatus.BAD_REQUEST),
    EMPTY_FILE("SYSS-1202", "File import không có dữ liệu", HttpStatus.BAD_REQUEST),
    ORDER_IMPORT_HAS_ERRORS("SYSS-1203", "File import có dòng lỗi", HttpStatus.UNPROCESSABLE_ENTITY),
    ERROR_FILE_NOT_FOUND("SYSS-1204", "File chứa lỗi không tồn tại", HttpStatus.NOT_FOUND),
    TEMPLATE_NOT_FOUND("SYSS-1205", "Template không tồn tại", HttpStatus.NOT_FOUND),

    // ===== WAREHOUSE =====
    WAREHOUSE_NOT_FOUND("SYSS-1300", "Kho hàng không tồn tại trong hệ thống", HttpStatus.NOT_FOUND),
    WAREHOUSE_NO_AVAILABLE_SLOT("SYSS-1301", "Kho không còn slot trống", HttpStatus.CONFLICT),

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
