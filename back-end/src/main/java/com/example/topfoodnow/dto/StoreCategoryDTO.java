package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "店家與分類關聯資料")
public class StoreCategoryDTO {
    @Schema(description = "店家ID", example = "1")
    private Integer storeId;

    @Schema(description = "分類ID", example = "5")
    private Integer categoryId;

    @Schema(description = "店家名稱", example = "好吃的滷肉飯")
    private String storeName;

    @Schema(description = "分類名稱", example = "中式料理")
    private String categoryName;
}