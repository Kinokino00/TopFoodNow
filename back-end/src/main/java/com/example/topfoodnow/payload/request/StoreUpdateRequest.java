package com.example.topfoodnow.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Set;

@Data
@Schema(description = "更新店家請求資料")
public class StoreUpdateRequest {
    @Schema(description = "店家ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id; // 通常更新會帶上ID

    @NotBlank(message = "店家名稱不能為空")
    @Size(max = 255, message = "店家名稱長度不能超過255個字符")
    @Schema(description = "店家名稱", example = "更新後的咖啡店名稱")
    private String name;

    @NotBlank(message = "店家地址不能為空")
    @Size(max = 255, message = "店家地址長度不能超過255個字符")
    @Schema(description = "店家地址", example = "台北市大安區忠孝東路三段2號")
    private String address;

    // 用於接收前端上傳的新圖片檔案
    @Schema(description = "店家圖片檔案列表 (新增上傳)", type = "string", format = "binary")
    private List<MultipartFile> newPhotoFiles;

    // 用於接收前端傳回的，現有的圖片 URL 列表
    // 前端應該將所有保留的圖片 URL 發送回來。
    // 任何不在這個列表中的舊 URL 都將被視為需要刪除。
    @Schema(description = "店家當前保留的圖片URL列表 (已上傳至GCS的完整URL)")
    private List<String> existingPhotoUrls;

    // 用於接收類別ID
    @Schema(description = "店家類別ID列表", example = "[1, 3]")
    private Set<Integer> categoryIds;
}