package com.ele.javademo.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
  private List<T> content;
  private long totalElements;
  private int page;
  private int size;

  public static <T> PagedResponse<T> of(List<T> content, long totalElements, int page, int size) {
    return PagedResponse.<T>builder()
        .content(content)
        .totalElements(totalElements)
        .page(page)
        .size(size)
        .build();
  }
}
