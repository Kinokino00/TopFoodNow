package com.example.topfoodnow.controller.store.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "包含店家ID、名稱和平均評分的響應對象")
@NoArgsConstructor
public class StoreWithAvgScoreResponse {
    @Schema(description = "推薦ID", example = "1")
    private Integer id;

    @Schema(description = "使用者ID", example = "1")
    private Integer userId;

    @Schema(description = "店家ID", example = "1")
    private Integer storeId;

    @Schema(description = "店家名稱", example = "好吃餐廳")
    private String storeName;

    @Schema(description = "店家平均評分", example = "4")
    private Integer score;

    @Schema(description = "店家最新推薦首圖", example = "https://url")
    private String photoUrl;

    public StoreWithAvgScoreResponse(Integer recommendId, Integer userId, Integer storeId, String storeName, Integer score) {
        this.id = recommendId;
        this.userId = userId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.score = score;
        this.photoUrl = null;
    }

    public StoreWithAvgScoreResponse(Integer recommendId, Integer userId, Integer storeId, String storeName, Integer score, String photoUrl) {
        this.id = recommendId;
        this.userId = userId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.score = score;
        this.photoUrl = photoUrl;
    }
}