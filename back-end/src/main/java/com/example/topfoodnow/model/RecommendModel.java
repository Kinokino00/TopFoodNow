package com.example.topfoodnow.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

@Entity
@Table(name = "recommend")
@Data
@Schema(description = "用戶推薦資料")
public class RecommendModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "推薦ID (主鍵)", example = "1")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) // 多對一，延遲載入
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(hidden = true) // 在 Schema 中隱藏，因為 userId 和 user 都是指同一個概念
    private UserModel user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
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


    // 多對多關聯到 CategoryModel
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "recommend_category", // 中間表
        joinColumns = @JoinColumn(name = "recommend_id"),      // 本實體 (RecommendModel) 在中間表中的外鍵
        inverseJoinColumns = @JoinColumn(name = "category_id") // 對應實體 (CategoryModel) 在中間表中的外鍵
    )
    @Schema(description = "此推薦相關的分類列表", hidden = true) // 在DTO中通常會處理這個列表，所以可以在Model中隱藏
    private Set<CategoryModel> categories = new HashSet<>(); // 一個推薦可以有多個分類

    // 建議新增的便捷方法，用於添加和移除分類
    public void addCategory(CategoryModel category) {
        this.categories.add(category);
        category.getRecommends().add(this);
    }

    public void removeCategory(CategoryModel category) {
        this.categories.remove(category);
        category.getRecommends().remove(this);
    }
}