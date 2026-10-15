package com.ele.javademo.exceptions;

import com.ele.javademo.business_code.CommonCode;
import com.ele.javademo.dto.ApiResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<@NotNull ApiResponse<Void>> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    ApiResponse<Void> response = ApiResponse.error(ex.getCode(), ex.getMessage());
    return ResponseEntity.ok(response);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<@NotNull ApiResponse<Void>> handleDuplicateResourceException(
      DuplicateResourceException ex) {
    ApiResponse<Void> response = ApiResponse.error(ex.getCode(), ex.getMessage());
    return ResponseEntity.ok(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<@NotNull ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getDefaultMessage())
        .filter(java.util.Objects::nonNull)
        .findFirst()
        .orElse("Dữ liệu không hợp lệ");
    return ResponseEntity.ok(ApiResponse.error(CommonCode.VALIDATION_ERROR.getCode(), message));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<@NotNull ApiResponse<Void>> handleUnreadableRequest(HttpMessageNotReadableException ex) {
    return ResponseEntity.ok(ApiResponse.error(CommonCode.VALIDATION_ERROR.getCode(), "Dữ liệu gửi lên không hợp lệ"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<@NotNull ApiResponse<Void>> handleGlobalException(Exception ex) {
    ApiResponse<Void> response =
        ApiResponse.error(
            CommonCode.INTERNAL_ERROR.getCode(),
            "Đã xảy ra lỗi không mong muốn: " + ex.getMessage());
    return ResponseEntity.ok(response);
  }
}
