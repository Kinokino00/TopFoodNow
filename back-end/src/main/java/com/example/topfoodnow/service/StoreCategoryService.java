package com.example.topfoodnow.service;

import com.example.topfoodnow.dto.StoreCategoryDTO;
import com.example.topfoodnow.dto.StoreCategoryResponseDTO;
import com.example.topfoodnow.dto.CategoryDTO;
import java.util.List;

public interface StoreCategoryService {
    StoreCategoryDTO addStoreCategory(Integer storeId, Integer categoryId);

    // 批量添加或更新分類，並帶有管理員標誌
    StoreCategoryResponseDTO addOrUpdateStoreCategories(Integer storeId, List<Integer> categoryIds, boolean isAdmin);

    void removeStoreCategory(Integer storeId, Integer categoryId);

    // 排序後的 CategoryDTO 列表
    StoreCategoryResponseDTO getSortedCategoriesForStore(Integer storeId);

    List<StoreCategoryDTO> getStoresByCategoryId(Integer categoryId);
}