package com.example.topfoodnow.controller.store.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "獲取指定店家詳細資訊的請求對象")
public class GetStoreDetailsRequest {
    @NotNull(message = "店家ID不能為空")
    @Positive(message = "店家ID必須大於0")
    @Schema(description = "店家ID", example = "1")
    private Integer storeId;
}
