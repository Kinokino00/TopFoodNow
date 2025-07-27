package com.example.topfoodnow.service.impl;

import com.example.topfoodnow.service.StoreService;
import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.model.CategoryModel;
import com.example.topfoodnow.repository.CategoryRepository;
import com.example.topfoodnow.repository.StoreRepository;
import com.example.topfoodnow.payload.request.StoreCreateRequest;
import com.example.topfoodnow.payload.request.StoreUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<StoreModel> findRandom3Stores() {
        return storeRepository.findRandom3Stores();
    }

    @Override
    @Transactional // 確保多個資料庫操作在一個事務中
    public Optional<StoreModel> getStoreById(Integer storeId) {
        Optional<StoreModel> storeOptional = storeRepository.findById(storeId);
        return storeOptional;
    }

    /**
     * 創建一個新店家，只處理店家基本資訊和類別關聯。
     * 圖片上傳現在應該由 RecommendService 或其他服務處理。
     * @param request 包含店家詳細資訊和類別ID的請求 DTO
     * @return 創建並保存後的店家實體
     */
    @Override
    @Transactional
    public StoreModel createStore(StoreCreateRequest request) {
        StoreModel store = new StoreModel();
        store.setName(request.getName());
        store.setAddress(request.getAddress());

        // 處理類別
        Set<CategoryModel> categories = new HashSet<>();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            categories = request.getCategoryIds().stream()
                    .map(categoryId -> categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + categoryId)))
                    .collect(Collectors.toSet());
        }
        store.setCategories(categories);
        return storeRepository.save(store);
    }

    /**
     * 更新現有店家資料，包括名稱、地址和類別。
     * 圖片更新現在應該由 RecommendService 或其他服務處理。
     * @param storeId 要更新的店家ID
     * @param request 包含更新數據的 DTO
     * @return 更新後的店家實體
     * @throws IllegalArgumentException 如果店家或類別不存在
     */
    @Override
    @Transactional
    public StoreModel updateStore(Integer storeId, StoreUpdateRequest request) {
        StoreModel existingStore = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with ID: " + storeId));
        existingStore.setName(request.getName());
        existingStore.setAddress(request.getAddress());

        if (request.getCategoryIds() != null) {
            Set<CategoryModel> updatedCategories = new HashSet<>();
            if (!request.getCategoryIds().isEmpty()) {
                updatedCategories = request.getCategoryIds().stream()
                        .map(categoryId -> categoryRepository.findById(categoryId)
                                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + categoryId)))
                        .collect(Collectors.toSet());
            }
            existingStore.setCategories(updatedCategories);
        }
        return storeRepository.save(existingStore);
    }

    /**
     * 刪除店家。圖片刪除邏輯應該在 RecommendService 中處理。
     * @param storeId 要刪除的店家ID
     * @throws IllegalArgumentException 如果未找到店家
     */
    @Override
    @Transactional
    public void deleteStore(Integer storeId) {
        StoreModel storeToDelete = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with ID: " + storeId));
        storeRepository.delete(storeToDelete);
    }
}