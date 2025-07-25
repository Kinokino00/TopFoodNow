// src/main/java/com/example/topfoodnow/service/impl/StoreCategoryServiceImpl.java
package com.example.topfoodnow.service.impl;

import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.model.CategoryModel;
import com.example.topfoodnow.model.StoreCategoryId;
import com.example.topfoodnow.model.StoreCategoryModel;
import com.example.topfoodnow.dto.CategoryDTO;
import com.example.topfoodnow.dto.StoreCategoryResponseDTO;
import com.example.topfoodnow.repository.StoreRepository;
import com.example.topfoodnow.repository.StoreCategoryRepository;
import com.example.topfoodnow.repository.CategoryRepository;
import com.example.topfoodnow.service.StoreCategoryService;
import com.example.topfoodnow.dto.StoreCategoryDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class StoreCategoryServiceImpl implements StoreCategoryService {
    private static final Logger logger = LoggerFactory.getLogger(StoreCategoryServiceImpl.class);

    private final StoreCategoryRepository storeCategoryRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;

    public StoreCategoryServiceImpl(
            StoreCategoryRepository storeCategoryRepository,
            StoreRepository storeRepository,
            CategoryRepository categoryRepository) {
        this.storeCategoryRepository = storeCategoryRepository;
        this.storeRepository = storeRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public StoreCategoryDTO addStoreCategory(Integer storeId, Integer categoryId) {
        // 檢查店家和分類是否存在
        StoreModel store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));
        CategoryModel category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

        // 檢查關聯是否已存在
        if (storeCategoryRepository.existsById_StoreIdAndId_CategoryId(storeId, categoryId)) {
            logger.warn("Store {} is already associated with category {}.", storeId, categoryId);
            return convertToDto(storeCategoryRepository.findById_StoreIdAndId_CategoryId(storeId, categoryId).get());
        }

        StoreCategoryModel storeCategory = new StoreCategoryModel(store, category, false);
        storeCategoryRepository.save(storeCategory);

        return convertToDto(storeCategory);
    }

    @Override
    @Transactional
    public StoreCategoryResponseDTO addOrUpdateStoreCategories(Integer storeId, List<Integer> categoryIds, boolean isAdmin) {
        // 檢查店家是否存在
        StoreModel store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found with ID: " + storeId));

        // 用於收集要返回的分類 DTO
        List<CategoryDTO> addedCategories = new ArrayList<>();

        for (Integer categoryId : categoryIds) {
            CategoryModel category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + categoryId));

            // 檢查關聯是否已存在
            Optional<StoreCategoryModel> existingAssociation = storeCategoryRepository.findById_StoreIdAndId_CategoryId(storeId, categoryId);

            StoreCategoryModel storeCategory;
            if (existingAssociation.isPresent()) {
                // 如果關聯已存在，更新其 isAdminAdded 狀態 (如果管理員再次添加，則保持為 true)
                storeCategory = existingAssociation.get();
                // 只有當當前是管理員添加，並且原有不是管理員添加時，才將其標記為管理員添加
                if (isAdmin && !storeCategory.getIsAdminAdded()) {
                    storeCategory.setIsAdminAdded(true);
                    logger.info("更新店家 {} 與分類 {} 的關聯，標記為管理員添加。", storeId, categoryId);
                }
            } else {
                // 如果關聯不存在，則創建新的關聯
                storeCategory = new StoreCategoryModel(store, category, isAdmin);
                logger.info("為店家 {} 添加分類 {} (管理員添加: {})。", storeId, categoryId, isAdmin);
            }
            storeCategoryRepository.save(storeCategory);
            addedCategories.add(convertToCategoryDTO(category));
        }

        // 返回更新後店家所有排序的分類列表
        return getSortedCategoriesForStore(storeId);
    }

    @Override
    @Transactional
    public void removeStoreCategory(Integer storeId, Integer categoryId) {
        if (!storeCategoryRepository.existsById_StoreIdAndId_CategoryId(storeId, categoryId)) {
            throw new RuntimeException("Store-Category association not found for Store ID: " + storeId + ", Category ID: " + categoryId);
        }
        storeCategoryRepository.deleteById_StoreIdAndId_CategoryId(storeId, categoryId);
        logger.info("已從店家 ID: {} 移除分類 ID: {} 的關聯。", storeId, categoryId);
    }

    @Override
    public StoreCategoryResponseDTO getSortedCategoriesForStore(Integer storeId) {
        StoreModel store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found with ID: " + storeId));

        // 1. 獲取直接關聯的分類 (來自 store_category 表)
        List<StoreCategoryModel> directAssociations = storeCategoryRepository.findStoreCategoriesByStoreIdWithDetails(storeId);

        // 將直接關聯的分類按 isAdminAdded 排序 (管理員添加的在前)
        // 使用 LinkedHashSet 來保持順序並去重
        Set<CategoryDTO> sortedCategories = new LinkedHashSet<>();

        // 首先添加所有管理員添加的分類
        directAssociations.stream()
                .filter(StoreCategoryModel::getIsAdminAdded)
                .map(sc -> convertToCategoryDTO(sc.getCategory()))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            list.sort(Comparator.comparing(CategoryDTO::getId)); // 如果多個管理員添加，按ID排序
                            return list;
                        }
                )).forEach(sortedCategories::add);

        // 2. 獲取用戶推薦次數最高的分類 (來自 recommend 相關表)
        // 這裡的邏輯需要您根據實際的 recommend 和 recommend_category 表結構來實現
        // 假設您在 StoreCategoryRepository 中有一個 findCategoriesByRecommendationCountForStore 方法
        List<Object[]> recommendedCategoryData = storeCategoryRepository.findCategoriesByRecommendationCountForStore(storeId);

        // 將推薦的分類（非管理員添加的且未重複的）添加到列表中
        recommendedCategoryData.stream()
                .map(row -> {
                    Integer categoryId = (Integer) row[0];
                    String categoryName = (String) row[1];
                    // 您可能需要查詢 CategoryModel 來獲取完整的 CategoryDTO
                    CategoryModel categoryModel = categoryRepository.findById(categoryId).orElse(null);
                    return categoryModel != null ? convertToCategoryDTO(categoryModel) : null;
                })
                .filter(dto -> dto != null && !sortedCategories.contains(dto)) // 排除已存在的管理員添加分類
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            // 這裡可以根據推薦次數再次排序，因為 native query 已經排過序了，但如果需要進一步處理
                            // 例如：list.sort(Comparator.comparingInt(CategoryDTO::getUserRecommendationCount).reversed());
                            return list;
                        }
                )).forEach(sortedCategories::add);


        // 最後添加所有剩餘的非管理員直接關聯的分類 (如果還未被包含)
        directAssociations.stream()
                .filter(sc -> !sc.getIsAdminAdded()) // 只考慮非管理員添加的
                .map(sc -> convertToCategoryDTO(sc.getCategory()))
                .filter(dto -> !sortedCategories.contains(dto)) // 排除已存在的分類
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            list.sort(Comparator.comparing(CategoryDTO::getId)); // 按ID排序
                            return list;
                        }
                )).forEach(sortedCategories::add);


        StoreCategoryResponseDTO responseDTO = new StoreCategoryResponseDTO();
        responseDTO.setStoreId(store.getId());
        responseDTO.setStoreName(store.getName()); // 假設 StoreModel 有 getName() 方法
        responseDTO.setCategories(new ArrayList<>(sortedCategories));

        return responseDTO;
    }

    @Override
    public List<StoreCategoryDTO> getStoresByCategoryId(Integer categoryId) {
        // 這裡的邏輯可以保持不變，因為它返回的是 StoreCategoryDTO，而不是排序後的 CategoryDTO 列表
        return storeCategoryRepository.findById_CategoryId(categoryId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 將 StoreCategoryModel 轉換為 StoreCategoryDTO
    private StoreCategoryDTO convertToDto(StoreCategoryModel model) {
        StoreCategoryDTO dto = new StoreCategoryDTO();
        dto.setStoreId(model.getId().getStoreId());
        dto.setCategoryId(model.getId().getCategoryId());
        if (model.getStore() != null) {
            dto.setStoreName(model.getStore().getName());
        }
        if (model.getCategory() != null) {
            dto.setCategoryName(model.getCategory().getName());
        }
        return dto;
    }

    // 將 CategoryModel 轉換為 CategoryDTO
    private CategoryDTO convertToCategoryDTO(CategoryModel model) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(model.getId());
        dto.setName(model.getName());
        return dto;
    }
}