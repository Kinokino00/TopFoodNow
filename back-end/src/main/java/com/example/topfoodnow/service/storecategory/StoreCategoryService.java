package com.example.topfoodnow.service.storecategory;

import com.example.topfoodnow.controller.store.response.StoreAndCategoryResponse;
import com.example.topfoodnow.controller.store.response.StoreCategoryListResponse;

import java.util.List;

public interface StoreCategoryService {
    StoreAndCategoryResponse addStoreCategory(Integer storeId, Integer categoryId);

    // 批量添加或更新分類，並帶有管理員標誌
    StoreCategoryListResponse addOrUpdateStoreCategories(Integer storeId, List<Integer> categoryIds, boolean isAdmin);

    void removeStoreCategory(Integer storeId, Integer categoryId);

    // 排序後的 CategoryDTO 列表
    StoreCategoryListResponse getSortedCategoriesForStore(Integer storeId);

    List<StoreAndCategoryResponse> getStoresByCategoryId(Integer categoryId);
}