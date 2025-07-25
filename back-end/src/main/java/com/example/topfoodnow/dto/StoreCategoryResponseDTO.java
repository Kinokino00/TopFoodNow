package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "店家分類列表響應對象")
public class StoreCategoryResponseDTO {
    @Schema(description = "店家ID", example = "1")
    private Integer storeId;

    @Schema(description = "店家名稱", example = "滷肉飯專賣店")
    private String storeName;

    @Schema(description = "店家經過排序的分類列表", example = "[{\"id\": 6, \"name\": \"中式料理\"}, {\"id\": 7, \"name\": \"小吃\"}, {\"id\": 1, \"name\": \"素食\"}]")
    private List<CategoryDTO> categories;
}