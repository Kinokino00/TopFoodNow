package com.example.topfoodnow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommend_user")
@IdClass(RecommendId.class) // 指定複合主鍵類別
@Data
public class RecommendModel {
    @Id
    @ManyToOne // 多對一
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @Id
    @ManyToOne // 多對一
    @JoinColumn(name = "store_id", nullable = false)
    private StoreModel store;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @PrePersist // 在持久化（插入）之前執行
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}