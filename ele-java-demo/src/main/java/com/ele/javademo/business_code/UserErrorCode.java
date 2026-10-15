package com.ele.javademo.business_code;

import lombok.Getter;

@Getter
public enum UserErrorCode {
  USER_NOT_FOUND("USER_01", "Không tìm thấy người dùng", false),
  USER_ALREADY_EXISTS("USER_02", "Người dùng đã tồn tại", false),
  EMAIL_ALREADY_EXISTS("USER_03", "Email đã được sử dụng", false),
  INVALID_USER_DATA("USER_04", "Dữ liệu người dùng không hợp lệ", true);

  private final String code;
  private final String message;
  private final boolean silent;

  UserErrorCode(String code, String message, boolean silent) {
    this.code = code;
    this.message = message;
    this.silent = silent;
  }
}
