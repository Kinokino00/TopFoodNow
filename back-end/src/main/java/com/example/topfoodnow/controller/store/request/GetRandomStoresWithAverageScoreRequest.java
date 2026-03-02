package com.example.topfoodnow.controller.store.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "獲取隨機店家及其平均評分的請求對象")
public class GetRandomStoresWithAverageScoreRequest {
    @Positive(message = "數量必須大於0")
    @Schema(description = "要取得的店家數量", example = "3")
    private int limit;
}
