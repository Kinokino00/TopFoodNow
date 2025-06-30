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

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendDTO {
    private Integer userId;

    private Integer storeId;

    private String userName;

    @NotNull(message = "店家名稱為必填！")
    private String storeName;

    @NotNull(message = "店家地址為必填！")
    private String storeAddress;

    private MultipartFile storePhoto;

    private String storePhotoUrl;

    @NotBlank(message = "推薦原因為必填！")
    private String reason;

    @NotNull(message = "請給店家評分！")
    @Min(value = 1, message = "評分最少為1顆星！")
    @Max(value = 5, message = "評分最多為5顆星！")
    private Integer score;

    private LocalDateTime createdAt;
}