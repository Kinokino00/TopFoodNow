package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "用於新增或更新推薦的請求數據")
public class RecommendRequestDTO {
    @Schema(description = "推薦的唯一ID，更新時使用，新增時無需提供", accessMode = Schema.AccessMode.READ_WRITE)
    private Integer id;

    @Schema(description = "店家的唯一ID，更新時必須提供，新增時可選（如果已存在）", accessMode = Schema.AccessMode.READ_WRITE)
    private Integer storeId;

    @Schema(description = "店家名稱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storeName;

    @Schema(description = "店家地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storeAddress;

    @ArraySchema(schema = @Schema(type = "string", format = "binary"), maxItems = 5)
    @Schema(description = "店家圖片，用於文件上傳", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<MultipartFile> storePhoto;

    @Size(max = 5, message = "最多只能上傳五張圖片")
    @Schema(description = "店家圖片的URL列表，用於顯示或保留現有圖片", accessMode = Schema.AccessMode.READ_WRITE)
    private List<String> storePhotoUrls;

    @Schema(description = "推薦原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @Schema(description = "推薦評分 (1-5)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer score;

    @Schema(description = "推薦的分類ID列表")
    private List<Integer> categoryIds;
}