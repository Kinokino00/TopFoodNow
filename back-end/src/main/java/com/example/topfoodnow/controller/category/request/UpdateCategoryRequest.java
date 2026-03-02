package com.example.topfoodnow.controller.category.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateCategoryRequest {

  @Schema(description = "分類ID", example = "1")
  @JsonProperty("id")
  @NotNull(message = "分類ID不可為空")
  private Integer id;

  @Schema(description = "分類名稱", example = "中式料理")
  @JsonProperty("categoryName")
  @Size(max = 50, message = "分類名稱不能超過50個字")
  @NotBlank(message = "分類名稱不可為空")
  private String categoryName;
}
