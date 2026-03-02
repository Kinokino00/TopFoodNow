package com.example.topfoodnow.controller.category.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;
import lombok.ToString;

@Setter
@ToString
public class UpdateCategoryResponse {

  @Schema(description = "分類ID", example = "1")
  @JsonProperty("id")
  private Integer id;

  @Schema(description = "分類名稱", example = "中式料理")
  @JsonProperty("categoryName")
  private String categoryName;
}
