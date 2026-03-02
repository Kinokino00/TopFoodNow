package com.example.topfoodnow.controller.category.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeleteCategoryRequest {

  @Schema(description = "分類ID", example = "1")
  @JsonProperty("id")
  @NotNull(message = "分類ID不可為空")
  private Integer id;
}
