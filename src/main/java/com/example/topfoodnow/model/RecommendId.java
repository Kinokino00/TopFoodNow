package com.example.topfoodnow.model;

import java.io.Serializable;
import java.util.Objects; // 導入 Objects 類，用於生成 equals 和 hashCode
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 複合主鍵類必須實現 Serializable 接口
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendId implements Serializable {
    private Integer user;  // 對應 RecommendModel 中的 user_id
    private Integer store; // 對應 RecommendModel 中的 store_id

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecommendId that = (RecommendId) o;
        return Objects.equals(user, that.user) && Objects.equals(store, that.store);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, store);
    }
}