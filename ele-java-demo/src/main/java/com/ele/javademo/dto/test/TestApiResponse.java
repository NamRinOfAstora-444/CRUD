package com.ele.javademo.dto.test;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestApiResponse {
  @NotBlank(message = "Tên không được để trống")
  @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
  private String name;

  @NotBlank(message = "Email không được để trống")
  @Email(message = "Email không hợp lệ")
  @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
  private String email;
}
