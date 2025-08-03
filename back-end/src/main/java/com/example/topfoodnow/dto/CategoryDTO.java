package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "分類資料")
public class CategoryDTO {
    @Schema(description = "分類ID", example = "1")
    private Integer id;

    @Schema(description = "分類名稱", example = "中式料理")
    private String categoryName;

    public CategoryDTO(Integer id, String categoryName) {
        this.id = id;
        this.categoryName = categoryName;
    }
}