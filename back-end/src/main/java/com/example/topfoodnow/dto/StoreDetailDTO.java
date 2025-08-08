package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Schema(description = "指定店家詳細資訊的響應物件")
@NoArgsConstructor
@AllArgsConstructor
public class StoreDetailDTO {
    @Schema(description = "店家 ID", example = "1")
    private Integer id;

    @Schema(description = "店家名稱", example = "好吃餐廳")
    private String storeName;

    @Schema(description = "店家地址", example = "台北市信義區")
    private String address;

    @Schema(description = "店家平均評分", example = "4")
    private Integer averageScore;

    @Schema(description = "店家所有分類名稱列表", example = "[\"中式料理\", \"日式料理\"]")
    private List<String> categoryNames;
}