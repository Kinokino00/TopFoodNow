package com.example.topfoodnow.service.impl;

import com.example.topfoodnow.dto.RecommendDTO;
import com.example.topfoodnow.dto.CategoryDTO;
import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.model.RecommendModel;
import com.example.topfoodnow.model.CategoryModel;
import com.example.topfoodnow.service.RecommendService;
import com.example.topfoodnow.repository.UserRepository;
import com.example.topfoodnow.repository.StoreRepository;
import com.example.topfoodnow.repository.RecommendRepository;
import com.example.topfoodnow.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendServiceImpl.class);

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final RecommendRepository recommendRepository;
    private final CategoryRepository categoryRepository;

    // region 新增推薦
    @Override
    @Transactional
    public void addRecommend(RecommendDTO recommendDTO, UserModel currentUserFromSession) {
        UserModel managedUser = userRepository.findById(currentUserFromSession.getId()).orElseThrow(() -> {
            logger.error("新增推薦失敗：用戶 ID {} 不存在或會話已過期", currentUserFromSession.getId());
            return new EntityNotFoundException("用戶不存在或會話已過期");
        });
        logger.info("為用戶 ID: {} (Email: {}) 嘗試新增推薦", managedUser.getId(), managedUser.getEmail());

        StoreModel store = storeRepository.findByName(recommendDTO.getStoreName()).orElseGet(() -> {
            StoreModel newStore = new StoreModel();
            newStore.setName(recommendDTO.getStoreName());
            newStore.setAddress(recommendDTO.getStoreAddress());
            newStore.setPhotoUrl(recommendDTO.getStorePhotoUrl());
            logger.info("新增新店家: {}", newStore.getName());
            return storeRepository.save(newStore);
        });

        if (recommendRepository.findByUserIdAndStoreId(managedUser.getId(), store.getId()).isPresent()) {
            logger.warn("用戶 ID: {} 已對店家 ID: {} 有推薦，避免重複新增", managedUser.getId(), store.getId());
            throw new IllegalArgumentException("您已經推薦過這家餐廳了！");
        }

        RecommendModel recommend = new RecommendModel();
        recommend.setUser(managedUser);
        recommend.setStore(store);
        recommend.setReason(recommendDTO.getReason());
        recommend.setScore(recommendDTO.getScore());

        // 處理分類
        if (recommendDTO.getCategoryIds() != null && !recommendDTO.getCategoryIds().isEmpty()) {
            Set<CategoryModel> categories = new HashSet<>();
            for (Integer categoryId : recommendDTO.getCategoryIds()) {
                CategoryModel category = categoryRepository.findById(categoryId).orElseThrow(() ->
                        new EntityNotFoundException("分類 ID " + categoryId + " 不存在")
                );
                categories.add(category);
            }
            recommend.setCategories(categories);
        }

        recommendRepository.save(recommend);
        logger.info("成功為用戶 ID: {} 新增推薦，店家ID: {}", managedUser.getId(), store.getId());
    }
    // endregion

    // region 取得用戶的所有推薦
    @Override
    @Transactional(readOnly = true)
    public List<RecommendDTO> getRecommendsByUserId(Integer userId) {
        UserModel managedUser = userRepository.findById(userId).orElseThrow(() -> {
            logger.error("取得推薦失敗：用戶 ID {} 不存在", userId);
            return new EntityNotFoundException("用戶不存在");
        });
        // 這裡需要更新 Repository 方法以避免 N+1 問題
        return recommendRepository.findByUserWithUserAndStoreAndCategories(managedUser.getId()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    // endregion

    // region 精確取得用戶對店家的推薦，用於所有推薦詳情頁面
    @Override
    @Transactional(readOnly = true)
    public Optional<RecommendDTO> getRecommendByUserAndStoreId(Integer userId, Integer storeId) {
        UserModel user = userRepository.findById(userId).orElseThrow(() -> {
            logger.error("查詢推薦失敗：用戶 ID {} 不存在", userId);
            return new EntityNotFoundException("用戶不存在");
        });

        Optional<StoreModel> storeOptional = storeRepository.findById(storeId);
        if (storeOptional.isEmpty()) {
            logger.warn("查詢推薦失敗：店家 ID {} 不存在", storeId);
            return Optional.empty();
        }
        Optional<RecommendModel> recommendModelOptional = recommendRepository.findByUserAndStoreIdWithUserAndStoreAndCategories(user.getId(), storeId);
        return recommendModelOptional.map(this::convertToDto);
    }
    // endregion

    // region 更新推薦
    @Override
    @Transactional
    public void updateRecommend(RecommendDTO recommendDTO, UserModel currentUserFromSession) {
        UserModel managedUser = userRepository.findById(currentUserFromSession.getId()).orElseThrow(() -> {
            logger.error("更新推薦失敗：用戶 ID {} 不存在或會話已過期", currentUserFromSession.getId());
            return new EntityNotFoundException("用戶不存在或會話已過期");
        });
        logger.info("嘗試為用戶 ID: {} (Email: {}) 更新推薦", managedUser.getId(), managedUser.getEmail());
        StoreModel storeToUpdate = storeRepository.findById(recommendDTO.getStoreId()).orElseThrow(() -> {
            logger.error("更新推薦失敗：要更新的店家不存在，店家ID: {}.", recommendDTO.getStoreId());
            return new EntityNotFoundException("要更新的店家不存在。店家ID: " + recommendDTO.getStoreId());
        });

        // 查找現有的推薦，確保它是當前用戶的
        RecommendModel existingRecommend = recommendRepository.findByUserAndStoreIdWithUserAndStoreAndCategories(managedUser.getId(), storeToUpdate.getId()).orElseThrow(() -> {
            logger.error("更新推薦失敗：找不到用戶 ID {} 對店家 ID {} 的推薦或您無權編輯", managedUser.getId(), recommendDTO.getStoreId());
            return new EntityNotFoundException("找不到該推薦或您無權編輯。用戶ID: " + managedUser.getId() + ", 店家ID: " + recommendDTO.getStoreId());
        });

        // 更新店家資訊
        storeToUpdate.setName(recommendDTO.getStoreName());
        storeToUpdate.setAddress(recommendDTO.getStoreAddress());
        if (recommendDTO.getStorePhotoUrl() != null && !recommendDTO.getStorePhotoUrl().isEmpty()) {
            storeToUpdate.setPhotoUrl(recommendDTO.getStorePhotoUrl());
        }
        storeRepository.save(storeToUpdate);

        // 更新推薦原因和評分
        existingRecommend.setReason(recommendDTO.getReason());
        existingRecommend.setScore(recommendDTO.getScore());

        // 更新分類
        if (recommendDTO.getCategoryIds() != null) {
            Set<CategoryModel> updatedCategories = new HashSet<>();
            for (Integer categoryId : recommendDTO.getCategoryIds()) {
                CategoryModel category = categoryRepository.findById(categoryId).orElseThrow(() ->
                        new EntityNotFoundException("分類 ID " + categoryId + " 不存在")
                );
                updatedCategories.add(category);
            }
            existingRecommend.setCategories(updatedCategories);
        } else {
            existingRecommend.setCategories(new HashSet<>());
        }

        recommendRepository.save(existingRecommend);
        logger.info("成功更新用戶 ID: {} 對店家 ID: {} 的推薦", managedUser.getId(), recommendDTO.getStoreId());
    }
    // endregion

    // region 刪除推薦
    @Override
    @Transactional
    public void deleteRecommend(Integer userId, Integer storeId, UserModel currentUserFromSession) {
        UserModel managedUser = userRepository.findById(currentUserFromSession.getId()).orElseThrow(() -> {
            logger.error("刪除推薦失敗：用戶 ID {} 不存在或會話已過期", currentUserFromSession.getId());
            return new EntityNotFoundException("用戶不存在或會話已過期");
        });

        RecommendModel recommendToDelete = recommendRepository.findByUserAndStoreIdWithUserAndStoreAndCategories(managedUser.getId(), storeId).orElseThrow(() -> {
            logger.error("刪除推薦失敗：找不到用戶 ID {} 對店家 ID {} 的推薦或您無權刪除", managedUser.getId(), storeId);
            return new EntityNotFoundException("找不到該推薦或您無權刪除");
        });

        recommendRepository.delete(recommendToDelete);
        logger.info("用戶 ID: {} 刪除了對店家 ID: {} 的推薦", managedUser.getId(), storeId);
    }
    // endregion

    // region 所有推薦
    @Override
    public Page<RecommendDTO> findAllRecommendsPaged(int page, int size, String searchTerm) {
        Pageable pageable = PageRequest.of(page, size);
        // 更新 Repository 方法以避免 N+1 問題
        Page<RecommendModel> recommendModelPage = recommendRepository.findFilteredRecommendsWithUserAndStoreAndCategories(searchTerm, pageable);
        return recommendModelPage.map(this::convertToDto);
    }
    // endregion

    // region 取隨機 3 筆推薦
    @Override
    @Transactional(readOnly = true)
    public List<RecommendDTO> findRandom3Recommends() {
        return recommendRepository.findRandom3RecommendsWithUserAndStoreAndCategories().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    // endregion

    // region 將 RecommendModel 轉換為 RecommendDTO
    private RecommendDTO convertToDto(RecommendModel recommendModel) {
        RecommendDTO dto = new RecommendDTO();

        // 用戶資訊轉換
        if (recommendModel.getUser() != null) {
            dto.setUserId(recommendModel.getUser().getId());
            dto.setName(recommendModel.getUser().getName());
        } else {
            dto.setUserId(null);
            dto.setName("未知用戶");
        }

        // 店家資訊轉換
        if (recommendModel.getStore() != null) {
            dto.setStoreId(recommendModel.getStore().getId());
            dto.setStoreName(recommendModel.getStore().getName());
            dto.setStoreAddress(recommendModel.getStore().getAddress());
            dto.setStorePhotoUrl(recommendModel.getStore().getPhotoUrl());
        } else {
            dto.setStoreId(null);
            dto.setStoreName("未知店家");
            dto.setStoreAddress("未知地址");
            dto.setStorePhotoUrl("/images/default-image.jpg");
        }

        // 取得並排序該店家最受歡迎的分類
        if (recommendModel.getStore() != null && recommendModel.getStore().getId() != null) {
            List<CategoryDTO> sortedCategories = recommendRepository.findCategoriesByStorePopularity(recommendModel.getStore().getId());
            dto.setCategories(new HashSet<>(sortedCategories));
            List<Integer> sortedCategoryIds = sortedCategories.stream()
                    .map(CategoryDTO::getId)
                    .collect(Collectors.toList());
            dto.setCategoryIds(sortedCategoryIds);
        } else {
            dto.setCategories(new HashSet<>());
            dto.setCategoryIds(new java.util.ArrayList<>());
        }

        dto.setReason(recommendModel.getReason());
        dto.setScore(recommendModel.getScore());
        dto.setCreatedAt(recommendModel.getCreatedAt());
        return dto;
    }
    // endregion
}