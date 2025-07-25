package com.example.topfoodnow.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_category")
@Data
@NoArgsConstructor
public class StoreCategoryModel {
    @EmbeddedId // 使用內嵌 ID 來表示複合主鍵
    private StoreCategoryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("storeId") // 將 StoreCategoryId 中的 storeId 映射到此 ManyToOne 關聯
    @JoinColumn(name = "store_id")
    private StoreModel store;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId") // 將 StoreCategoryId 中的 categoryId 映射到此 ManyToOne 關聯
    @JoinColumn(name = "category_id")
    private CategoryModel category;

    @Column(name = "is_admin_added")
    private Boolean isAdminAdded = false;

    public StoreCategoryModel(StoreModel store, CategoryModel category, Boolean isAdminAdded) {
        this.store = store;
        this.category = category;
        if (store != null && category != null) {
            this.id = new StoreCategoryId(store.getId(), category.getId());
        } else {
            this.id = null;
        }
        this.isAdminAdded = isAdminAdded;
    }

    // 用於默認為非管理員添加的情況
    public StoreCategoryModel(StoreModel store, CategoryModel category) {
        this(store, category, false);
    }
}