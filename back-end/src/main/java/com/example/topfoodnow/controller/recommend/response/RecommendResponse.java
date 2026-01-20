package com.example.topfoodnow.controller.recommend.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Data
@Schema(description = "返回給前端的推薦響應數據")
public class RecommendResponse {
    @Schema(description = "推薦的唯一ID")
    private Integer id;

    @Schema(description = "推薦的用戶ID")
    private Integer userId;

    @Schema(description = "用戶名稱")
    private String userName;

    @Schema(description = "店家的唯一ID")
    private Integer storeId;

    @Schema(description = "店家名稱")
    private String storeName;

    @Schema(description = "店家地址")
    private String storeAddress;

    @Schema(description = "推薦原因")
    private String reason;

    @Schema(description = "推薦評分 (1-5)")
    private Integer score;

    @Schema(description = "推薦的分類名稱列表")
    private List<String> categoryNames;

    @Schema(description = "推薦創建時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "推薦的首張圖片URL")
    private List<String> photoUrls;
}