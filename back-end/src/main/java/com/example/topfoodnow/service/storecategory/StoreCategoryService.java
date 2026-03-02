package com.example.topfoodnow.service.storecategory;

import com.example.topfoodnow.controller.category.response.GetAllCategoriesData;
import com.example.topfoodnow.controller.storecategory.response.GetStoreAndCategoryResponse;
import com.example.topfoodnow.controller.storecategory.response.GetStoreCategoriesResponse;
import com.example.topfoodnow.infra.category.Category;
import com.example.topfoodnow.infra.category.CategoryRepository;
import com.example.topfoodnow.infra.store.Store;
import com.example.topfoodnow.infra.store.StoreRepository;
import com.example.topfoodnow.infra.storecategory.StoreCategory;
import com.example.topfoodnow.infra.storecategory.StoreCategoryRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreCategoryService {
    private final StoreCategoryRepository storeCategoryRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public GetStoreAndCategoryResponse addStoreCategory(Integer storeId, Integer categoryId) {
        // 檢查店家和分類是否存在
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

        // 檢查關聯是否已存在
        if (storeCategoryRepository.existsById_StoreIdAndId_CategoryId(storeId, categoryId)) {
            log.warn("Store {} is already associated with category {}.", storeId, categoryId);
            return convertToDto(storeCategoryRepository.findById_StoreIdAndId_CategoryId(storeId, categoryId).get());
        }

        StoreCategory storeCategory = new StoreCategory(store, category, false);
        storeCategoryRepository.save(storeCategory);

        return convertToDto(storeCategory);
    }

    @Override
    @Transactional
    public GetStoreCategoriesResponse addOrUpdateStoreCategories(Integer storeId, List<Integer> categoryIds,
            boolean isAdmin) {
        // 檢查店家是否存在
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found with ID: " + storeId));

        // 用於收集要返回的分類 DTO
        List<GetAllCategoriesData> addedCategories = new ArrayList<>();

        for (Integer categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + categoryId));

            // 檢查關聯是否已存在
            Optional<StoreCategory> existingAssociation = storeCategoryRepository
                    .findById_StoreIdAndId_CategoryId(storeId, categoryId);

            StoreCategory storeCategory;
            if (existingAssociation.isPresent()) {
                // 如果關聯已存在，更新其 isAdminAdded 狀態 (如果管理員再次添加，則保持為 true)
                storeCategory = existingAssociation.get();
                // 只有當當前是管理員添加，並且原有不是管理員添加時，才將其標記為管理員添加
                if (isAdmin && !storeCategory.getIsAdminAdded()) {
                    storeCategory.setIsAdminAdded(true);
                    log.info("更新店家 {} 與分類 {} 的關聯，標記為管理員添加。", storeId, categoryId);
                }
            } else {
                // 如果關聯不存在，則創建新的關聯
                storeCategory = new StoreCategory(store, category, isAdmin);
                log.info("為店家 {} 添加分類 {} (管理員添加: {})。", storeId, categoryId, isAdmin);
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
            throw new RuntimeException(
                    "Store-Category association not found for Store ID: " + storeId + ", Category ID: " + categoryId);
        }
        storeCategoryRepository.deleteById_StoreIdAndId_CategoryId(storeId, categoryId);
        log.info("已從店家 ID: {} 移除分類 ID: {} 的關聯。", storeId, categoryId);
    }

    @Override
    public GetStoreCategoriesResponse getSortedCategoriesForStore(Integer storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found with ID: " + storeId));

        // 1. 取得直接關聯的分類 (來自 store_category 表)
        List<StoreCategory> directAssociations = storeCategoryRepository
                .findStoreCategoriesByStoreIdWithDetails(storeId);

        // 將直接關聯的分類按 isAdminAdded 排序 (管理員添加的在前)
        // 使用 LinkedHashSet 來保持順序並去重
        Set<GetAllCategoriesData> sortedCategories = new LinkedHashSet<>();

        // 首先添加所有管理員添加的分類
        directAssociations.stream()
                .filter(StoreCategory::getIsAdminAdded)
                .map(sc -> convertToCategoryDTO(sc.getCategory()))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            list.sort(Comparator.comparing(GetAllCategoriesData::getId));
                            return list;
                        }))
                .forEach(sortedCategories::add);

        // 2. 取得用戶推薦次數最高的分類 (來自 recommend 相關表)
        List<Object[]> recommendedCategoryData = storeCategoryRepository
                .findCategoriesByRecommendationCountForStore(storeId);

        // 將推薦的分類（非管理員添加的且未重複的）添加到列表中
        recommendedCategoryData.stream()
                .map(row -> {
                    Integer categoryId = (Integer) row[0];
                    String categoryName = (String) row[1];
                    // 您可能需要查詢 CategoryModel 來取得完整的 CategoryDTO
                    Category category = categoryRepository.findById(categoryId).orElse(null);
                    return category != null ? convertToCategoryDTO(category) : null;
                })
                .filter(dto -> dto != null && !sortedCategories.contains(dto))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            return list;
                        }))
                .forEach(sortedCategories::add);

        // 最後添加所有剩餘的非管理員直接關聯的分類
        directAssociations.stream()
                .filter(sc -> !sc.getIsAdminAdded()) // 只考慮非管理員添加的
                .map(sc -> convertToCategoryDTO(sc.getCategory()))
                .filter(dto -> !sortedCategories.contains(dto)) // 排除已存在的分類
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            list.sort(Comparator.comparing(GetAllCategoriesData::getId)); // 按ID排序
                            return list;
                        }))
                .forEach(sortedCategories::add);

        GetStoreCategoriesResponse responseDTO = new GetStoreCategoriesResponse();
        responseDTO.setStoreId(store.getId());
        responseDTO.setStoreName(store.getName());
        responseDTO.setCategories(new ArrayList<>(sortedCategories));

        return responseDTO;
    }

    @Override
    public List<GetStoreAndCategoryResponse> getStoresByCategoryId(Integer categoryId) {
        return storeCategoryRepository.findById_CategoryId(categoryId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private GetStoreAndCategoryResponse convertToDto(StoreCategory model) {
        GetStoreAndCategoryResponse dto = new GetStoreAndCategoryResponse();
        dto.setStoreId(model.getId().getStoreId());
        dto.setCategoryId(model.getId().getCategoryId());
        if (model.getStore() != null) {
            dto.setStoreName(model.getStore().getName());
        }
        if (model.getCategory() != null) {
            dto.setCategoryName(model.getCategory().getCategoryName());
        }
        return dto;
    }

    // 將 CategoryModel 轉換為 CategoryDTO
    private GetAllCategoriesData convertToCategoryDTO(Category model) {
        GetAllCategoriesData dto = new GetAllCategoriesData();
        dto.setId(model.getId());
        dto.setCategoryName(model.getCategoryName());
        return dto;
    }
}