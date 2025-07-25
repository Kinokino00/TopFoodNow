package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分類資料")
public class CategoryDTO {
    @Schema(description = "分類ID", example = "1")
    private Integer id;

    @Schema(description = "分類名稱", example = "中式料理")
    private String name;

    public CategoryDTO(Integer id, String name, Long popularityCount) {
        this.id = id;
        this.name = name;
    }
}