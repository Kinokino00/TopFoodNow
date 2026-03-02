package com.example.topfoodnow.controller.category.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class DeleteCategoryResponse {
  private String message;
  private Boolean success;
}
