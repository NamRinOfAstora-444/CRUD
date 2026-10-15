package com.ele.javademo.exceptions;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {
  private final String code;

  public DuplicateResourceException(String code, String message) {
    super(message);
    this.code = code;
  }
}
