package com.example.topfoodnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "推薦信息資料傳輸對象")
public class RecommendDTO {
    @Schema(description = "用戶ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer userId;

    @Schema(description = "店家ID", example = "101", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer storeId;

    @Schema(description = "用戶名稱", example = "小王", accessMode = Schema.AccessMode.READ_ONLY)
    private String name;

    @NotNull(message = "店家名稱為必填！")
    @Schema(description = "店家名稱", example = "四木欣福")
    private String storeName;

    @NotNull(message = "店家地址為必填！")
    @Schema(description = "店家地址", example = "台北市信義區忠孝東路一段1號")
    private String storeAddress;

    @Schema(description = "店家照片")
    private MultipartFile storePhoto;

    @Schema(description = "店家照片URL", example = "http://example.com/images/store101.jpg", accessMode = Schema.AccessMode.READ_ONLY)
    private String storePhotoUrl;

    @NotBlank(message = "推薦原因為必填！")
    @Schema(description = "推薦原因", example = "這家滷肉飯真的太好吃了，份量足，CP值高！")
    private String reason;

    @NotNull(message = "請給店家評分！")
    @Min(value = 1, message = "評分最少為1顆星！")
    @Max(value = 5, message = "評分最多為5顆星！")
    @Schema(description = "推薦評分 (1-5星)", example = "5")
    private Integer score;

    @Schema(description = "創建時間", example = "2023-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    // 用於接收前端傳入的分類ID列表（在新增/更新推薦時）
    @Schema(description = "相關分類ID列表", example = "[1, 3, 5]")
    private List<Integer> categoryIds;

    @Schema(description = "相關分類列表", accessMode = Schema.AccessMode.READ_ONLY)
    private Set<CategoryDTO> categories = new HashSet<>(); // 確保初始化避免NPE
}