package com.ele.javademo.dto;

import com.ele.javademo.business_code.CommonCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
  private String code;
  private String message;
  private T result;

  public static <T> ApiResponse<T> success(T result) {
    return ApiResponse.<T>builder()
        .code(CommonCode.SUCCESS.getCode())
        .message(CommonCode.SUCCESS.getMessage())
        .result(result)
        .build();
  }

  public static <T> ApiResponse<T> success(String message, T result) {
    return ApiResponse.<T>builder()
        .code(CommonCode.SUCCESS.getCode())
        .message(message)
        .result(result)
        .build();
  }

  public static <T> ApiResponse<T> error(CommonCode commonCode) {
    return ApiResponse.<T>builder()
        .code(commonCode.getCode())
        .message(commonCode.getMessage())
        .build();
  }

  public static <T> ApiResponse<T> error(CommonCode commonCode, String customMessage) {
    return ApiResponse.<T>builder().code(commonCode.getCode()).message(customMessage).build();
  }

  public static <T> ApiResponse<T> error(String code, String message) {
    return ApiResponse.<T>builder().code(code).message(message).result(null).build();
  }
}
