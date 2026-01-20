package com.example.topfoodnow.infra.storecategory;

import com.example.topfoodnow.infra.category.Category;
import com.example.topfoodnow.infra.store.Store;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_category")
@Data
@NoArgsConstructor
public class StoreCategory {
    @EmbeddedId // 使用內嵌 ID 來表示複合主鍵
    private StoreCategoryPK id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("storeId") // 將 StoreCategoryId 中的 storeId 映射到此 ManyToOne 關聯
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId") // 將 StoreCategoryId 中的 categoryId 映射到此 ManyToOne 關聯
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "is_admin_added")
    private Boolean isAdminAdded = false;

    public StoreCategory(Store store, Category category, Boolean isAdminAdded) {
        this.store = store;
        this.category = category;
        if (store != null && category != null) {
            this.id = new StoreCategoryPK(store.getId(), category.getId());
        } else {
            this.id = null;
        }
        this.isAdminAdded = isAdminAdded;
    }

    // 用於默認為非管理員添加的情況
    public StoreCategory(Store store, Category category) {
        this(store, category, false);
    }
}