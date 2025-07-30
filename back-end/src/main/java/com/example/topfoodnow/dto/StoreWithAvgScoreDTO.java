package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Schema(description = "包含店家ID、名稱和平均評分的響應對象")
@NoArgsConstructor
@AllArgsConstructor
public class StoreWithAvgScoreDTO {
    @Schema(description = "店家ID", example = "1")
    private Integer id;

    @Schema(description = "店家名稱", example = "好吃餐廳")
    private String name;

    @Schema(description = "店家平均評分", example = "4.5")
    private Double averageScore;
}