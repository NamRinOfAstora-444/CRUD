package com.ele.javademo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class Test {
  // TEST CASE ERROR

  @GetMapping("/internal-error")
  public void testInternalError() {
    throw new RuntimeException("Test lỗi hệ thống - đây là exception cố ý");
  }

  @GetMapping("/null-pointer")
  public void testNullPointer() {
    String test = null;
    test.length(); // Throws NullPointerException
  }
}
