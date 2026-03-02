package com.example.topfoodnow.controller.store.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "獲取隨機店家及其平均評分的響應對象")
public class GetRandomStoresWithAverageScoreResponse {
    @Schema(description = "隨機店家列表及其平均評分", example = "[{\"id\": 1, \"name\": \"店家1\", \"avgScore\": 4.5}]")
    private List<StoreWithAvgScoreResponse> stores;
}
