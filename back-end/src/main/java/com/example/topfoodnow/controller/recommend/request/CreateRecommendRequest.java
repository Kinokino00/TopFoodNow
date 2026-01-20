package com.example.topfoodnow.controller.recommend.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.springframework.web.multipart.MultipartFile;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "用於新增推薦的請求數據")
public class CreateRecommendRequest {

    @Schema(description = "店家名稱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storeName;

    @Schema(description = "店家地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storeAddress;

    @ArraySchema(schema = @Schema(type = "string", format = "binary"), maxItems = 5)
    @Schema(description = "店家圖片，用於文件上傳", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<MultipartFile> storePhoto;

    @Schema(description = "店家圖片的URL列表，通常用於顯示或保留現有圖片", accessMode = Schema.AccessMode.READ_ONLY) // 新增時為只讀
    private List<String> storePhotoUrl;

    @Schema(description = "推薦原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @Schema(description = "推薦評分 (1-5星)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer score;

    @Schema(description = "推薦的分類ID列表")
    private List<Integer> categoryIds;
}