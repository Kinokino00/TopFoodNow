package com.example.topfoodnow.model;

import java.io.Serializable;
import java.util.Objects; // 導入 Objects 類，用於生成 equals 和 hashCode

// 複合主鍵類必須實現 Serializable 接口
public class RecommendId implements Serializable {
    private Integer userId;  // 對應 RecommendModel 中的 user_id
    private Integer storeId; // 對應 RecommendModel 中的 store_id

    public RecommendId() {}
    public RecommendId(Integer userId, Integer storeId) {
        this.userId = userId;
        this.storeId = storeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecommendId)) return false;
        RecommendId that = (RecommendId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(storeId, that.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, storeId);
    }
}