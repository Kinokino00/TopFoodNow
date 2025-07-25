package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "為店家添加多個分類的請求對象")
public class StoreCategoryRequestDTO {
    @NotNull(message = "店家ID不能為空")
    @Schema(description = "店家ID", example = "1")
    private Integer storeId;

    @NotNull(message = "分類ID列表不能為空")
    @Size(min = 1, message = "分類ID列表不能為空")
    @Schema(description = "要添加的分類ID列表", example = "[6, 7]")
    private List<Integer> categoryIds;
}