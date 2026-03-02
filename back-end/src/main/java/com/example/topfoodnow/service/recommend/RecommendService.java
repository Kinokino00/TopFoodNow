package com.example.topfoodnow.service.recommend;

import com.example.topfoodnow.controller.recommend.request.CreateRecommendRequest;
import com.example.topfoodnow.controller.recommend.request.UpdateRecommendRequest;
import com.example.topfoodnow.controller.recommend.response.GetRecommendResponse;
import com.example.topfoodnow.infra.category.Category;
import com.example.topfoodnow.infra.category.CategoryRepository;
import com.example.topfoodnow.infra.recommend.Recommend;
import com.example.topfoodnow.infra.recommend.RecommendRepository;
import com.example.topfoodnow.infra.store.Store;
import com.example.topfoodnow.infra.store.StoreRepository;
import com.example.topfoodnow.infra.user.User;
import com.example.topfoodnow.service.filestorage.FileStorageService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {
  private final RecommendRepository recommendRepository;
  private final StoreRepository storeRepository;
  private final CategoryRepository categoryRepository;
  private final FileStorageService fileStorageService;

    // 將 RecommendModel 轉換為 RecommendResponseDTO
    private GetRecommendResponse convertToResponseDTO(Recommend recommend) {
        GetRecommendResponse dto = new GetRecommendResponse();
        dto.setId(recommend.getId());
        dto.setReason(recommend.getReason());
        dto.setScore(recommend.getScore());
        dto.setCreatedAt(recommend.getCreatedAt());

        if (recommend.getUser() != null) {
            dto.setUserId(recommend.getUser().getId());
            dto.setUserName(recommend.getUser().getName());
        }

        if (recommend.getStore() != null) {
            dto.setStoreId(recommend.getStore().getId());
            dto.setStoreName(recommend.getStore().getName());
            dto.setStoreAddress(recommend.getStore().getAddress());
        }

        if (recommend.getCategories() != null && !recommend.getCategories().isEmpty()) {
            dto.setCategoryNames(recommend.getCategories().stream()
                    .map(Category::getCategoryName)
                    .collect(Collectors.toList()));
        } else {
            dto.setCategoryNames(new ArrayList<>());
        }

        if (recommend.getPhotoUrls() != null && !recommend.getPhotoUrls().isEmpty()) {
            dto.setPhotoUrls(new ArrayList<>(recommend.getPhotoUrls()));
        } else {
            dto.setPhotoUrls(new ArrayList<>());
        }

        return dto;
    }

    @Override
    @Transactional
    public Page<GetRecommendResponse> findAllRecommendsPaged(Pageable pageable, String searchTerm) {
        Specification<Recommend> spec = (root, query, criteriaBuilder) -> {
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
                    Root<Recommend> subRoot = categorySubquery.from(Recommend.class);
                    Join<Recommend, Category> categoryJoin = subRoot.join("categories", JoinType.LEFT);
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

        Page<Recommend> recommendModelsPage = recommendRepository.findAll(spec, pageable);

        List<RecommendResponse> content = recommendModelsPage.getContent().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, recommendModelsPage.getTotalElements());
    }

    @Override
    @Transactional
    public GetRecommendResponse addRecommend(CreateRecommendRequest requestDTO, List<String> uploadedPhotoUrls, User currentUser) {
        log.info("為用戶 ID: {} (Email: {}) 嘗試新增推薦", currentUser.getId(), currentUser.getEmail());

        // 查找或創建店家
        Store store = storeRepository.findByName(requestDTO.getStoreName())
                .orElseGet(() -> {
                    Store newStore = new Store();
                    newStore.setName(requestDTO.getStoreName());
                    newStore.setAddress(requestDTO.getStoreAddress());
                    return storeRepository.save(newStore);
                });

        // 檢查是否已存在該用戶對該店家的推薦
        Optional<Recommend> existingRecommend = recommendRepository.findByUserAndStore(currentUser, store);
        if (existingRecommend.isPresent()) {
            throw new IllegalArgumentException("您已推薦過此店家。請考慮更新現有推薦。");
        }

        // 創建新的推薦模型
        Recommend recommend = new Recommend();
        recommend.setUser(currentUser);
        recommend.setStore(store); // 關聯店家
        recommend.setReason(requestDTO.getReason());
        recommend.setScore(requestDTO.getScore());
        recommend.setCreatedAt(LocalDateTime.now());
        recommend.setPhotoUrls(uploadedPhotoUrls);

        // 處理分類 ID
        if (requestDTO.getCategoryIds() != null && !requestDTO.getCategoryIds().isEmpty()) {
            List<Category> categoriesList = categoryRepository.findAllById(requestDTO.getCategoryIds());
            if (categoriesList.size() != requestDTO.getCategoryIds().size()) {
                throw new IllegalArgumentException("部分分類ID無效。");
            }
            // 將 List 轉換為 Set 設置給 RecommendModel
            recommend.setCategories(new HashSet<>(categoriesList));
        } else {
            recommend.setCategories(new HashSet<>());
        }

        recommend = recommendRepository.save(recommend);
        log.info("成功為用戶 ID: {} 新增推薦，店家ID: {}", currentUser.getId(), store.getId());
        return convertToResponseDTO(recommend);
    }

    @Override
    @Transactional
    public GetRecommendResponse updateRecommend(Integer recommendId, UpdateRecommendRequest recommendRequest, List<String> newUploadedPhotoUrls, List<String> retainedPhotoUrls, User currentUser) throws IOException {
        log.info("為用戶 ID: {} (Email: {}) 嘗試更新推薦，推薦ID: {}", currentUser.getId(), currentUser.getEmail(), recommendId);

        // 1. 根據 recommendId 找到現有的推薦 (使用傳入的 recommendId)
        Recommend existingRecommend = recommendRepository.findById(recommendId)
                .orElseThrow(() -> new EntityNotFoundException("未找到推薦 ID: " + recommendId));

        // 驗證用戶是否有權限更新此推薦
        if (existingRecommend.getUser().getId() != (currentUser.getId())) {
            throw new IllegalArgumentException("您無權修改此推薦。");
        }

        // 2. 更新店家資訊 (如果店家名稱或地址有變更)
        Store storeToUpdate = existingRecommend.getStore(); // 取得當前推薦關聯的店家
        if (storeToUpdate == null) {
            throw new EntityNotFoundException("推薦關聯的店家不存在。");
        }

        storeToUpdate.setName(recommendRequest.getStoreName());
        storeToUpdate.setAddress(recommendRequest.getStoreAddress());
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

        // 執行圖片刪除
        for (String url : urlsToDeleteFromGCS) {
            try {
                fileStorageService.deleteFile(url);
                log.info("成功刪除圖片 (更新時移除舊圖): {}", url);
            } catch (Exception e) {
                log.error("刪除圖片失敗 (更新時移除舊圖): {}. URL: {}", e.getMessage(), url, e);
                // 考慮是否要重新拋出異常或僅記錄
            }
        }

        // 構建最終的圖片 URL 列表
        List<String> finalPhotoUrls = new ArrayList<>(retainedPhotoUrls); // 從保留的舊圖片開始
        if (newUploadedPhotoUrls != null && !newUploadedPhotoUrls.isEmpty()) {
            finalPhotoUrls.addAll(newUploadedPhotoUrls); // 新圖片排在最後
        }

        // 4. 更新 RecommendModel 的 reason, score, photoUrls
        existingRecommend.setReason(recommendRequest.getReason());
        existingRecommend.setScore(recommendRequest.getScore());
        existingRecommend.setPhotoUrls(finalPhotoUrls); // 設置最終的圖片 URL 列表

        // 5. 處理分類 ID 更新
        if (recommendRequest.getCategoryIds() != null && !recommendRequest.getCategoryIds().isEmpty()) {
            List<Category> categoriesList = categoryRepository.findAllById(recommendRequest.getCategoryIds());
            if (categoriesList.size() != recommendRequest.getCategoryIds().size()) {
                throw new IllegalArgumentException("部分分類ID無效。");
            }
            existingRecommend.setCategories(new HashSet<>(categoriesList));
        } else {
            existingRecommend.setCategories(new HashSet<>());
        }

        // 6. 保存更新後的推薦
        existingRecommend = recommendRepository.save(existingRecommend);
        log.info("成功為用戶 ID: {} 更新推薦，推薦ID: {}", currentUser.getId(), existingRecommend.getId());

        return convertToResponseDTO(existingRecommend);
    }

    @Override
    @Transactional
    public void deleteRecommend(Integer userId, Integer storeId, User currentUser) throws IOException {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("未找到店家 ID: " + storeId));

        Recommend recommendToDelete = recommendRepository.findByUserAndStore(currentUser, store)
                .orElseThrow(() -> new EntityNotFoundException("未找到您對店家 ID: " + storeId + " 的推薦，或您無權刪除。"));

        // 1. 刪除此特定推薦關聯的 GCS 圖片
        List<String> photoUrls = recommendToDelete.getPhotoUrls();
        if (photoUrls != null && !photoUrls.isEmpty()) {
            for (String photoUrl : photoUrls) {
                try {
                    fileStorageService.deleteFile(photoUrl);
                    log.info("成功刪除圖片: {}", photoUrl);
                } catch (Exception e) {
                    log.error("刪除圖片失敗: {}. 圖片 URL: {}", e.getMessage(), photoUrl, e);
                    // 這裡可以選擇是否重新拋出異常或僅記錄，取決於業務需求
                }
            }
        }

        // 2. 刪除特定的 RecommendModel 記錄
        recommendRepository.delete(recommendToDelete);
        log.info("成功刪除用戶 ID: {} 對店家 ID: {} 的推薦。", userId, storeId);

        // 3. 檢查該店家是否還有任何推薦。如果沒有，則刪除店家。
        long remainingRecommendsCount = recommendRepository.countByStoreId(storeId);

        if (remainingRecommendsCount == 0) {
            storeRepository.delete(store);
            log.info("店家 ID: {} 已沒有任何推薦，已連同店家一併刪除。", storeId);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<GetRecommendResponse> getRecommendsByUserId(Integer userId) {
        List<Recommend> recommends = recommendRepository.findByUserId(userId);
        return recommends.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GetRecommendResponse> getRecommendByUserAndStoreId(Integer userId, Integer storeId) {
        Optional<Recommend> recommendOptional = recommendRepository.findByUserIdAndStoreId(userId, storeId);
        return recommendOptional.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetRecommendResponse> getRecommendsByStoreId(Integer storeId) {
        List<Recommend> recommends = recommendRepository.findByStoreId(storeId);
        return recommends.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}