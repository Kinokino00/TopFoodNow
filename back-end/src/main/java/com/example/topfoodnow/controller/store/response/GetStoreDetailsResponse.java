package com.example.topfoodnow.controller.store.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "獲取指定店家詳細資訊的響應對象")
public class GetStoreDetailsResponse {
    @Schema(description = "店家詳細資訊", example = "{\"id\": 1, \"name\": \"滷肉飯專賣店\", \"address\": \"台北市...\", \"avgScore\": 4.5}")
    private StoreDetailResponse storeDetail;
}
