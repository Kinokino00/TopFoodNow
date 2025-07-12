package com.example.topfoodnow.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data // 自動生成 getter/setter、toString、equals、hashCode
@NoArgsConstructor  // 自動生成無參數建構子
@AllArgsConstructor // 自動生成全參數建構子
@Schema(description = "AI 推薦餐廳資料")
public class AiRestaurantModel {
    @Schema(description = "餐廳名稱", example = "鼎泰豐")
    private String name;

    @Schema(description = "餐廳地址", example = "台北市信義路二段22號")
    private String address;

    private String photoUrl;

    @Schema(description = "餐廳官方網址", example = "https://www.dintaifung.com.tw/", nullable = true)
    private String url;
}
