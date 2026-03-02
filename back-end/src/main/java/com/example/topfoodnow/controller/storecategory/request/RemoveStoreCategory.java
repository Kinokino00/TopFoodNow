package com.example.topfoodnow.controller.storecategory.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "從店家移除分類的請求對象")
public class RemoveStoreCategoryRequest {
    @NotNull(message = "店家ID不能為空")
    @Schema(description = "店家ID", example = "1")
    private Integer storeId;

    @NotNull(message = "分類ID不能為空")
    @Schema(description = "分類ID", example = "5")
    private Integer categoryId;
}
