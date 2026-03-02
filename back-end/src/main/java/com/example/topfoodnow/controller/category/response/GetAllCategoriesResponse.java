package com.example.topfoodnow.controller.category.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@ToString
public class GetAllCategoriesResponse {

  @Schema(description = "所有分類列表")
  @JsonProperty("getAllCategoriesList")
  List<GetAllCategoriesData> getAllCategoriesList;
}
