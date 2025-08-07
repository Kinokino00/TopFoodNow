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
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.IOException;

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

    // 將 RecommendModel 轉換為 RecommendResponseDTO
    private RecommendResponseDTO convertToResponseDTO(RecommendModel recommendModel) {
        RecommendResponseDTO dto = new RecommendResponseDTO();
        dto.setId(recommendModel.getId());
        dto.setReason(recommendModel.getReason());
        dto.setScore(recommendModel.getScore());
        dto.setCreatedAt(recommendModel.getCreatedAt());

        if (recommendModel.getUser() != null) {
            dto.setUserId(recommendModel.getUser().getId());
            dto.setUserName(recommendModel.getUser().getName());
        }

        if (recommendModel.getStore() != null) {
            dto.setStoreId(recommendModel.getStore().getId());
            dto.setStoreName(recommendModel.getStore().getName());
            dto.setStoreAddress(recommendModel.getStore().getAddress());
        }

        if (recommendModel.getCategories() != null && !recommendModel.getCategories().isEmpty()) {
            dto.setCategoryNames(recommendModel.getCategories().stream()
                    .map(CategoryModel::getCategoryName)
                    .collect(Collectors.toList()));
        } else {
            dto.setCategoryNames(new ArrayList<>());
        }

        if (recommendModel.getPhotoUrls() != null && !recommendModel.getPhotoUrls().isEmpty()) {
            dto.setPhotoUrls(new ArrayList<>(recommendModel.getPhotoUrls()));
        } else {
            dto.setPhotoUrls(new ArrayList<>());
        }

        return dto;
    }

    @Override
    @Transactional
    public Page<RecommendResponseDTO> findAllRecommendsPaged(Pageable pageable, String searchTerm) {
        Specification<RecommendModel> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> finalPredicates = new ArrayList<>(); // 用於存放每個關鍵字的 OR 組合條件

            if (StringUtils.hasText(searchTerm)) {
                String[] keywords = searchTerm.toLowerCase().split("\\s+");

                for (String keyword : keywords) {
                    // 對於每個關鍵字，創建一個包含所有可能匹配字段的 OR 條件
                    Predicate currentKeywordOrPredicate = criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("user").<String>get("name")), "%" + keyword + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("store").<String>get("name")), "%" + keyword + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("store").<String>get("address")), "%" + keyword + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("reason")), "%" + keyword + "%")
                    );

                    // 針對分類的處理：檢查推薦的 categories 集合中是否存在符合條件的 category
                    Subquery<Integer> categorySubquery = query.subquery(Integer.class);
                    Root<RecommendModel> subRoot = categorySubquery.from(RecommendModel.class);
                    Join<RecommendModel, CategoryModel> categoryJoin = subRoot.join("categories", JoinType.LEFT);
                    categorySubquery.select(subRoot.get("id"))
                            .where(criteriaBuilder.and(
                                    criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                                    criteriaBuilder.like(criteriaBuilder.lower(categoryJoin.get("categoryName")), "%" + keyword + "%")
                            ));
                    Predicate categoryExistsPredicate = criteriaBuilder.exists(categorySubquery);

                    // 將當前關鍵字的基礎 OR 條件 和 分類 EXISTS 條件進行 OR 組合
                    Predicate combinedOrForCurrentKeyword = criteriaBuilder.or(currentKeywordOrPredicate, categoryExistsPredicate);

                    // 將這個組合條件添加到最終的 Predicates 列表中 (這裡的列表中的每個元素都是一個關鍵字的大 OR 條件)
                    finalPredicates.add(combinedOrForCurrentKeyword);
                }
                // 當 searchTerm 不為空時，我們使用 OR 來組合所有關鍵字的條件
                // 這表示只要滿足任一關鍵字的條件即可被找到
                return criteriaBuilder.or(finalPredicates.toArray(new Predicate[0]));

            } else {
                // 當 searchTerm 為空時，返回一個永遠為真的條件，以取得所有數據
                return criteriaBuilder.conjunction();
            }
        };

        Page<RecommendModel> recommendModelsPage = recommendRepository.findAll(spec, pageable);

        List<RecommendResponseDTO> content = recommendModelsPage.getContent().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, recommendModelsPage.getTotalElements());
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
        recommend.setPhotoUrls(uploadedPhotoUrls);

        // 處理分類 ID
        if (requestDTO.getCategoryIds() != null && !requestDTO.getCategoryIds().isEmpty()) {
            List<CategoryModel> categoriesList = categoryRepository.findAllById(requestDTO.getCategoryIds());
            if (categoriesList.size() != requestDTO.getCategoryIds().size()) {
                throw new IllegalArgumentException("部分分類ID無效。");
            }
            // 將 List 轉換為 Set 設置給 RecommendModel
            recommend.setCategories(new HashSet<>(categoriesList));
        } else {
            recommend.setCategories(new HashSet<>());
        }

        recommend = recommendRepository.save(recommend);
        logger.info("成功為用戶 ID: {} 新增推薦，店家ID: {}", currentUserModel.getId(), store.getId());
        return convertToResponseDTO(recommend);
    }

    @Override
    @Transactional
    public RecommendResponseDTO updateRecommend(Integer recommendId, RecommendRequestDTO recommendRequestDTO, List<String> newUploadedPhotoUrls, List<String> retainedPhotoUrls, UserModel currentUserModel) throws IOException {
        logger.info("為用戶 ID: {} (Email: {}) 嘗試更新推薦，推薦ID: {}", currentUserModel.getId(), currentUserModel.getEmail(), recommendId);

        // 1. 根據 recommendId 找到現有的推薦 (使用傳入的 recommendId)
        RecommendModel existingRecommend = recommendRepository.findById(recommendId)
                .orElseThrow(() -> new EntityNotFoundException("未找到推薦 ID: " + recommendId));

        // 驗證用戶是否有權限更新此推薦
        if (existingRecommend.getUser().getId() != (currentUserModel.getId())) {
            throw new IllegalArgumentException("您無權修改此推薦。");
        }

        // 2. 更新店家資訊 (如果店家名稱或地址有變更)
        StoreModel storeToUpdate = existingRecommend.getStore(); // 取得當前推薦關聯的店家
        if (storeToUpdate == null) {
            throw new EntityNotFoundException("推薦關聯的店家不存在。");
        }

        storeToUpdate.setName(recommendRequestDTO.getStoreName());
        storeToUpdate.setAddress(recommendRequestDTO.getStoreAddress());
        storeRepository.save(storeToUpdate); // 保存店家資訊的變更

        // 3. 處理圖片更新邏輯
        List<String> currentPhotoUrls = existingRecommend.getPhotoUrls(); // 從資料庫取得現有圖片 URL
        if (currentPhotoUrls == null) {
            currentPhotoUrls = new ArrayList<>();
        }

        // 確保 retainedPhotoUrls 不為空
        if (retainedPhotoUrls == null) {
            retainedPhotoUrls = new ArrayList<>();
        }

        // 找出需要從 GCS 刪除的圖片：存在於資料庫但不在前端希望保留列表中的
        List<String> urlsToDeleteFromGCS = new ArrayList<>();
        for (String url : currentPhotoUrls) {
            if (!retainedPhotoUrls.contains(url)) {
                urlsToDeleteFromGCS.add(url);
            }
        }

        // 執行 GCS 圖片刪除
        for (String url : urlsToDeleteFromGCS) {
            try {
                gcsService.deleteFile(url);
                logger.info("成功刪除 GCS 圖片 (更新時移除舊圖): {}", url);
            } catch (Exception e) {
                logger.error("刪除 GCS 圖片失敗 (更新時移除舊圖): {}. URL: {}", e.getMessage(), url, e);
                // 考慮是否要重新拋出異常或僅記錄
            }
        }

        // 構建最終的圖片 URL 列表
        List<String> finalPhotoUrls = new ArrayList<>(retainedPhotoUrls); // 從保留的舊圖片開始
        if (newUploadedPhotoUrls != null && !newUploadedPhotoUrls.isEmpty()) {
            finalPhotoUrls.addAll(newUploadedPhotoUrls); // 新圖片排在最後
        }

        // 4. 更新 RecommendModel 的 reason, score, photoUrls
        existingRecommend.setReason(recommendRequestDTO.getReason());
        existingRecommend.setScore(recommendRequestDTO.getScore());
        existingRecommend.setPhotoUrls(finalPhotoUrls); // 設置最終的圖片 URL 列表

        // 5. 處理分類 ID 更新
        if (recommendRequestDTO.getCategoryIds() != null && !recommendRequestDTO.getCategoryIds().isEmpty()) {
            List<CategoryModel> categoriesList = categoryRepository.findAllById(recommendRequestDTO.getCategoryIds());
            if (categoriesList.size() != recommendRequestDTO.getCategoryIds().size()) {
                throw new IllegalArgumentException("部分分類ID無效。");
            }
            existingRecommend.setCategories(new HashSet<>(categoriesList));
        } else {
            existingRecommend.setCategories(new HashSet<>());
        }

        // 6. 保存更新後的推薦
        existingRecommend = recommendRepository.save(existingRecommend);
        logger.info("成功為用戶 ID: {} 更新推薦，推薦ID: {}", currentUserModel.getId(), existingRecommend.getId());

        return convertToResponseDTO(existingRecommend);
    }

    @Override
    @Transactional
    public void deleteRecommend(Integer userId, Integer storeId, UserModel currentUserModel) throws IOException {
        StoreModel store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("未找到店家 ID: " + storeId));

        RecommendModel recommendToDelete = recommendRepository.findByUserAndStore(currentUserModel, store)
                .orElseThrow(() -> new EntityNotFoundException("未找到您對店家 ID: " + storeId + " 的推薦，或您無權刪除。"));

        // 1. 刪除此特定推薦關聯的 GCS 圖片
        List<String> photoUrls = recommendToDelete.getPhotoUrls();
        if (photoUrls != null && !photoUrls.isEmpty()) {
            for (String photoUrl : photoUrls) {
                try {
                    gcsService.deleteFile(photoUrl);
                    logger.info("成功刪除 GCS 圖片: {}", photoUrl);
                } catch (Exception e) {
                    logger.error("刪除 GCS 圖片失敗: {}. 圖片 URL: {}", e.getMessage(), photoUrl, e);
                    // 這裡可以選擇是否重新拋出異常或僅記錄，取決於業務需求
                }
            }
        }

        // 2. 刪除特定的 RecommendModel 記錄
        recommendRepository.delete(recommendToDelete);
        logger.info("成功刪除用戶 ID: {} 對店家 ID: {} 的推薦。", userId, storeId);

        // 3. 檢查該店家是否還有任何推薦。如果沒有，則刪除店家。
        long remainingRecommendsCount = recommendRepository.countByStoreId(storeId);

        if (remainingRecommendsCount == 0) {
            storeRepository.delete(store);
            logger.info("店家 ID: {} 已沒有任何推薦，已連同店家一併刪除。", storeId);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<RecommendResponseDTO> getRecommendsByUserId(Integer userId) {
        List<RecommendModel> recommends = recommendRepository.findByUserId(userId);
        return recommends.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RecommendResponseDTO> getRecommendByUserAndStoreId(Integer userId, Integer storeId) {
        Optional<RecommendModel> recommendOptional = recommendRepository.findByUserIdAndStoreId(userId, storeId);
        return recommendOptional.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendResponseDTO> getRecommendsByStoreId(Integer storeId) {
        List<RecommendModel> recommends = recommendRepository.findByStoreId(storeId);
        return recommends.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}