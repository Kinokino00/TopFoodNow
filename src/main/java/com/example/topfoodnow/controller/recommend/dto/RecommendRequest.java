package com.example.topfoodnow.controller.recommend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "推薦資料請求物件")
public class RecommendRequest {
    @Schema(description = "用戶ID", example = "1")
    private Integer userId;

    @Schema(description = "用戶名稱", example = "張三")
    private String userName;

    @Schema(description = "用戶電子郵件", example = "user@example.com")
    private String userEmail;

    @Schema(description = "用戶是否為名人", example = "true")
    private Boolean isFamous;

    @NotNull(message = "店家ID不能為空")
    @Schema(description = "店家ID", example = "101")
    private Integer storeId;

    @Schema(description = "店家名稱", example = "阿明滷肉飯")
    private String storeName;

    @Schema(description = "店家地址", example = "台北市中正區忠孝東路一段1號")
    private String storeAddress;

    @Schema(description = "店家照片網址", example = "/uploads/store123.jpg")
    private String storePhotoUrl;

    @NotBlank(message = "推薦原因不能為空")
    @Schema(description = "推薦原因", example = "這家滷肉飯真的太好吃了，份量足，CP值高！")
    private String reason;

    @NotNull(message = "評分不能為空")
    @Min(value = 1, message = "評分至少為1星")
    @Max(value = 5, message = "評分最多為5星")
    @Schema(description = "推薦評分 (1-5星)", example = "5")
    private Integer score;

    @Schema(description = "創建時間", example = "2023-01-15T10:30:00")
    private LocalDateTime createdAt;
}
