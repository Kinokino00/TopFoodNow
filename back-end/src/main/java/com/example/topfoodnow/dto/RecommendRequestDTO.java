package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "用於更新推薦的請求數據")
public class RecommendRequestDTO {
    @Schema(description = "要更新的推薦的唯一ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id; // 這個 id 就是 recommend 表中的 id

    @Schema(description = "店家的唯一ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer storeId; // 這個會從資料庫中取得，不是前端傳入的主要更新標識

    @Schema(description = "店家名稱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storeName;

    @Schema(description = "店家地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storeAddress;

    @ArraySchema(schema = @Schema(type = "string", format = "binary"), maxItems = 5)
    @Schema(description = "店家圖片，用於文件上傳", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<MultipartFile> storePhoto;

    @Schema(description = "推薦原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @Schema(description = "推薦評分 (1-5)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer score;

    @Schema(description = "推薦的分類ID列表")
    private List<Integer> categoryIds;
}