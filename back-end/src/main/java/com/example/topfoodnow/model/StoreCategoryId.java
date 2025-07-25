package com.example.topfoodnow.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

// 複合主鍵
@Embeddable // 表示這是一個可以內嵌到其他實體中的類
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreCategoryId implements Serializable {
    @Column(name = "store_id")
    private Integer storeId;

    @Column(name = "category_id")
    private Integer categoryId;
}