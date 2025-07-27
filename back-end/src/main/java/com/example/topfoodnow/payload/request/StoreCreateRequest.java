package com.example.topfoodnow.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Set;

@Data
@Schema(description = "創建店家請求資料")
public class StoreCreateRequest {
    @NotBlank(message = "店家名稱不能為空")
    @Size(max = 255, message = "店家名稱長度不能超過255個字符")
    @Schema(description = "店家名稱", example = "測試咖啡店")
    private String name;

    @NotBlank(message = "店家地址不能為空")
    @Size(max = 255, message = "店家地址長度不能超過255個字符")
    @Schema(description = "店家地址", example = "台北市大安區忠孝東路三段1號")
    private String address;

    // 用於接收前端上傳的多張圖片檔案
    @Schema(description = "店家圖片檔案列表 (新增時上傳)", type = "string", format = "binary")
    private List<MultipartFile> newPhotoFiles;

    // 用於接收類別ID
    @NotNull(message = "店家類別不能為空")
    @Schema(description = "店家類別ID列表", example = "[1, 2]")
    private Set<Integer> categoryIds;
}