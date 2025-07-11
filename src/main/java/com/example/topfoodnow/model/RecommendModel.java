package com.example.topfoodnow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "recommend_user")
@IdClass(RecommendId.class) // 指定複合主鍵類別
@Data
@Schema(description = "用戶推薦資料")
public class RecommendModel {
    @Id
    @Column(name = "user_id", nullable = false)
    @Schema(description = "用戶ID (複合主鍵一部分)", example = "1")
    private Integer userId;

    @Id
    @Column(name = "store_id", nullable = false)
    @Schema(description = "店家ID (複合主鍵一部分)", example = "101")
    private Integer storeId;

    @ManyToOne(fetch = FetchType.LAZY) // 多對一，延遲載入
    @MapsId("userId")
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @Schema(hidden = true) // 在 Schema 中隱藏，因為 userId 和 user 都是指同一個概念
    private UserModel user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("storeId")
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    @Schema(hidden = true)
    private StoreModel store;

    @Column(nullable = false, length = 500)
    @Schema(description = "推薦原因", example = "這家滷肉飯真的太好吃了，份量足，CP值高！")
    private String reason;

    @Column(nullable = false)
    @Schema(description = "推薦評分 (1-5星)", example = "5")
    private Integer score;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "創建時間", example = "2023-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    @PrePersist // 在持久化（插入）之前執行
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}