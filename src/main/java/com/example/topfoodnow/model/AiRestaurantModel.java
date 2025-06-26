package com.example.topfoodnow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // 自動生成 getter/setter、toString、equals、hashCode
@NoArgsConstructor  // 自動生成無參數建構子
@AllArgsConstructor // 自動生成全參數建構子
public class AiRestaurantModel {
    private String name;
    private String address;
    private String photoUrl;
    private String url;
}
