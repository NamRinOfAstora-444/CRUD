package com.ele.javademo.business_code;

import lombok.Getter;

@Getter
public enum CommonCode {
  SUCCESS("200", "Thao tác thành công", false),
  BAD_REQUEST("400", "Yêu cầu không hợp lệ", false),
  UNAUTHORIZED("401", "Chưa xác thực", false),
  FORBIDDEN("403", "Không có quyền truy cập", false),
  NOT_FOUND("404", "Không tìm thấy tài nguyên", false),
  CONFLICT("409", "Dữ liệu bị trùng lặp", false),
  VALIDATION_ERROR("422", "Dữ liệu không hợp lệ", false),
  INTERNAL_ERROR("500", "Lỗi hệ thống", false),
  SERVICE_UNAVAILABLE("503", "Dịch vụ tạm thời không khả dụng", false);

  private final String code;
  private final String message;
  private final boolean silent;

  CommonCode(String code, String message, boolean silent) {
    this.code = code;
    this.message = message;
    this.silent = silent;
  }
}
