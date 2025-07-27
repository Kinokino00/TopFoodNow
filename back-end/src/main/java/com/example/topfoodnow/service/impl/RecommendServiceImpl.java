package com.example.topfoodnow.service.impl;

import com.example.topfoodnow.dto.RecommendRequestDTO;
import com.example.topfoodnow.dto.RecommendResponseDTO;
import com.example.topfoodnow.dto.RecommendCreateRequestDTO;
import com.example.topfoodnow.model.RecommendModel;
import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.model.CategoryModel;
import com.example.topfoodnow.repository.RecommendRepository;
import com.example.topfoodnow.repository.StoreRepository;
import com.example.topfoodnow.repository.CategoryRepository;
import com.example.topfoodnow.service.GcsService;
import com.example.topfoodnow.service.RecommendService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendServiceImpl.class);

    @Autowired
    private RecommendRepository recommendRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private GcsService gcsService;

    /**
     * 將 RecommendModel 轉換為 RecommendResponseDTO
     */
    private RecommendResponseDTO convertToResponseDTO(RecommendModel recommendModel) {
        RecommendResponseDTO dto = new RecommendResponseDTO();
        dto.setId(recommendModel.getId());
        dto.setUserId(recommendModel.getUser().getId());
        dto.setStoreId(recommendModel.getStore().getId());
        dto.setStoreName(recommendModel.getStore().getName());
        dto.setStoreAddress(recommendModel.getStore().getAddress());
        dto.setReason(recommendModel.getReason());
        dto.setScore(recommendModel.getScore());
        dto.setCreatedAt(recommendModel.getCreatedAt());

        // 處理分類名稱
        if (recommendModel.getCategories() != null) {
            dto.setCategoryNames(recommendModel.getCategories().stream()
                    .map(CategoryModel::getName)
                    .collect(Collectors.toList()));
        } else {
            dto.setCategoryNames(new ArrayList<>());
        }

        return dto;
    }

    @Override
    @Transactional
    public RecommendResponseDTO addRecommend(RecommendCreateRequestDTO requestDTO, List<String> uploadedPhotoUrls, UserModel currentUserModel) {
        logger.info("為用戶 ID: {} (Email: {}) 嘗試新增推薦", currentUserModel.getId(), currentUserModel.getEmail());

        // 查找或創建店家
        StoreModel store = storeRepository.findByName(requestDTO.getStoreName())
                .orElseGet(() -> {
                    StoreModel newStore = new StoreModel();
                    newStore.setName(requestDTO.getStoreName());
                    newStore.setAddress(requestDTO.getStoreAddress());
                    return storeRepository.save(newStore);
                });

        // 檢查是否已存在該用戶對該店家的推薦
        Optional<RecommendModel> existingRecommend = recommendRepository.findByUserAndStore(currentUserModel, store);
        if (existingRecommend.isPresent()) {
            throw new IllegalArgumentException("您已推薦過此店家。請考慮更新現有推薦。");
        }

        // 創建新的推薦模型
        RecommendModel recommend = new RecommendModel();
        recommend.setUser(currentUserModel);
        recommend.setStore(store); // 關聯店家
        recommend.setReason(requestDTO.getReason());
        recommend.setScore(requestDTO.getScore());
        recommend.setCreatedAt(LocalDateTime.now());
        recommend.setPhotoUrls(uploadedPhotoUrls); // 將 Controller 傳來的 GCS URL 列表設置到 RecommendModel

        // 處理分類 ID
        if (requestDTO.getCategoryIds() != null && !requestDTO.getCategoryIds().isEmpty()) {
            List<CategoryModel> categoriesList = categoryRepository.findAllById(requestDTO.getCategoryIds());
            if (categoriesList.size() != requestDTO.getCategoryIds().size()) {
                // 如果查詢回來的分類數量不匹配請求的數量，說明有無效 ID
                throw new IllegalArgumentException("部分分類ID無效。");
            }
            // 將 List 轉換為 Set 設置給 RecommendModel
            recommend.setCategories(new HashSet<>(categoriesList));
        } else {
            recommend.setCategories(new HashSet<>()); // 沒有分類則設置為空 Set
        }

        // 保存推薦
        recommend = recommendRepository.save(recommend);
        logger.info("成功為用戶 ID: {} 新增推薦，店家ID: {}", currentUserModel.getId(), store.getId());

        // 轉換為響應 DTO 並返回
        return convertToResponseDTO(recommend);
    }

    @Override
    @Transactional
    // 保持簽名與介面一致，接收 List<String> newPhotoUrls
    public RecommendResponseDTO updateRecommend(RecommendRequestDTO recommendRequestDTO, List<String> newPhotoUrls, UserModel currentUserModel) {
        logger.info("為用戶 ID: {} (Email: {}) 嘗試更新推薦，店家ID: {}", currentUserModel.getId(), currentUserModel.getEmail(), recommendRequestDTO.getStoreId());

        if (recommendRequestDTO.getStoreId() == null) {
            throw new IllegalArgumentException("更新推薦必須提供店家ID。");
        }

        StoreModel storeToUpdate = storeRepository.findById(recommendRequestDTO.getStoreId())
                .orElseThrow(() -> new EntityNotFoundException("未找到店家 ID: " + recommendRequestDTO.getStoreId()));

        RecommendModel existingRecommend = recommendRepository.findByUserAndStore(currentUserModel, storeToUpdate)
                .orElseThrow(() -> new EntityNotFoundException("未找到您對店家 ID: " + recommendRequestDTO.getStoreId() + " 的推薦，或您無權修改。"));

        // 更新 StoreModel 的名稱和地址，StoreModel不再有photoUrl和createdAt
        storeToUpdate.setName(recommendRequestDTO.getStoreName());
        storeToUpdate.setAddress(recommendRequestDTO.getStoreAddress());
        storeRepository.save(storeToUpdate);

        // 獲取現有的圖片 URL
        List<String> currentPhotoUrls = existingRecommend.getPhotoUrls();
        if (currentPhotoUrls == null) {
            currentPhotoUrls = new ArrayList<>();
        }

        List<String> finalPhotoUrls = new ArrayList<>();

        // 1. 保留前端傳來的現有圖片 URL
        if (recommendRequestDTO.getStorePhotoUrls() != null) {
            finalPhotoUrls.addAll(recommendRequestDTO.getStorePhotoUrls());
        }

        // 2. 添加新上傳的圖片 URL
        if (newPhotoUrls != null && !newPhotoUrls.isEmpty()) {
            finalPhotoUrls.addAll(newPhotoUrls);
        }

        // 3. 找出需要刪除的舊圖片
        List<String> urlsToDelete = new ArrayList<>();
        for (String url : currentPhotoUrls) {
            if (!finalPhotoUrls.contains(url)) { // 如果舊的 URL 不在新集合中，則表示需要刪除
                urlsToDelete.add(url);
            }
        }

        // 執行 GCS 圖片刪除
        for (String url : urlsToDelete) {
            try {
                gcsService.deleteFile(url);
                logger.info("成功刪除 GCS 圖片 (更新時移除舊圖): {}", url);
            } catch (Exception e) {
                logger.error("刪除 GCS 圖片失敗 (更新時移除舊圖): {}. URL: {}", e.getMessage(), url, e);
            }
        }

        // 更新 RecommendModel 的 reason, score, photoUrls
        existingRecommend.setReason(recommendRequestDTO.getReason());
        existingRecommend.setScore(recommendRequestDTO.getScore());
        existingRecommend.setPhotoUrls(finalPhotoUrls); // 設置最終的圖片 URL 列表

        if (recommendRequestDTO.getCategoryIds() != null && !recommendRequestDTO.getCategoryIds().isEmpty()) {
            List<CategoryModel> categoriesList = categoryRepository.findAllById(recommendRequestDTO.getCategoryIds());
            if (categoriesList.size() != recommendRequestDTO.getCategoryIds().size()) {
                throw new IllegalArgumentException("部分分類ID無效。");
            }
            existingRecommend.setCategories(new HashSet<>(categoriesList));
        } else {
            existingRecommend.setCategories(new HashSet<>());
        }

        existingRecommend = recommendRepository.save(existingRecommend);
        logger.info("成功為用戶 ID: {} 更新推薦，店家ID: {}", currentUserModel.getId(), storeToUpdate.getId());

        return convertToResponseDTO(existingRecommend);
    }

    @Override
    public void deleteRecommend(Integer userId, Integer storeId, UserModel currentUserModel) {
        StoreModel store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("未找到店家 ID: " + storeId));
        RecommendModel recommend = recommendRepository.findByUserAndStore(currentUserModel, store)
                .orElseThrow(() -> new EntityNotFoundException("未找到您對店家 ID: " + storeId + " 的推薦，或您無權刪除。"));

        // 在刪除推薦時，也要刪除相關的 GCS 圖片
        if (recommend.getPhotoUrls() != null && !recommend.getPhotoUrls().isEmpty()) {
            for (String photoUrl : recommend.getPhotoUrls()) {
                try {
                    gcsService.deleteFile(photoUrl);
                    logger.info("成功刪除 GCS 圖片: {}", photoUrl);
                } catch (Exception e) {
                    logger.error("刪除 GCS 圖片失敗: {}. 圖片 URL: {}", e.getMessage(), photoUrl, e);
                    // 這裡可以選擇是否拋出異常或繼續，取決於業務需求
                }
            }
        }

        recommendRepository.delete(recommend);
        logger.info("成功刪除用戶 ID: {} 對店家 ID: {} 的推薦。", userId, storeId);
    }

    @Override
    public List<RecommendResponseDTO> getRecommendsByUserId(Integer userId) {
        List<RecommendModel> recommends = recommendRepository.findByUserId(userId);
        return recommends.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RecommendResponseDTO> getRecommendByUserAndStoreId(Integer userId, Integer storeId) {
        Optional<RecommendModel> recommendOptional = recommendRepository.findByUserIdAndStoreId(userId, storeId);
        return recommendOptional.map(this::convertToResponseDTO);
    }

    @Override
    public Page<RecommendResponseDTO> findAllRecommendsPaged(int page, int size, String[] sort) {
        Sort springSort = Sort.unsorted();
        if (sort != null && sort.length > 0) {
            try {
                String property = sort[0];
                Sort.Direction direction = Sort.Direction.ASC;
                if (sort.length > 1 && sort[1].equalsIgnoreCase("desc")) {
                    direction = Sort.Direction.DESC;
                }
                springSort = Sort.by(direction, property);
            } catch (Exception e) {
                logger.warn("解析排序參數失敗，使用預設排序。錯誤: {}", e.getMessage());
            }
        }
        Pageable pageable = PageRequest.of(page, size, springSort);
        Page<RecommendModel> recommendModelsPage = recommendRepository.findFilteredRecommendsWithUserAndStoreAndCategories("", pageable);

        List<RecommendResponseDTO> content = recommendModelsPage.getContent().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, recommendModelsPage.getTotalElements());
    }
}