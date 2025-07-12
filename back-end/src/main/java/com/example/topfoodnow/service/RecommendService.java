package com.example.topfoodnow.service;

import com.example.topfoodnow.dto.RecommendDTO;
import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.model.RecommendModel;
import com.example.topfoodnow.repository.UserRepository;
import com.example.topfoodnow.repository.StoreRepository;
import com.example.topfoodnow.repository.RecommendRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendService.class);

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final RecommendRepository recommendRepository;

    // 新增推薦
    @Transactional
    public void addRecommend(RecommendDTO recommendDTO, UserModel currentUserFromSession) {
        UserModel managedUser = userRepository.findById(currentUserFromSession.getId()).orElseThrow(() -> {
            logger.error("新增推薦失敗：用戶 ID {} 不存在或會話已過期。", currentUserFromSession.getId());
            return new EntityNotFoundException("用戶不存在或會話已過期。");
        });
        logger.info("為用戶 ID: {} (Email: {}) 嘗試新增推薦。", managedUser.getId(), managedUser.getEmail());

        StoreModel store = storeRepository.findByName(recommendDTO.getStoreName()).orElseGet(() -> {
            StoreModel newStore = new StoreModel();
            newStore.setName(recommendDTO.getStoreName());
            newStore.setAddress(recommendDTO.getStoreAddress());
            newStore.setPhotoUrl(recommendDTO.getStorePhotoUrl());
            logger.info("新增新店家: {}", newStore.getName());
            return storeRepository.save(newStore);
        });

        if (recommendRepository.findByUserIdAndStoreId(managedUser.getId(), store.getId()).isPresent()) {
            logger.warn("用戶 ID: {} 已對店家 ID: {} 有推薦，避免重複新增。", managedUser.getId(), store.getId());
            throw new IllegalArgumentException("您已經推薦過這家餐廳了！");
        }
        RecommendModel recommend = new RecommendModel();
        recommend.setUserId(managedUser.getId());
        recommend.setStoreId(store.getId());
        recommend.setUser(managedUser);
        recommend.setStore(store);
        recommend.setReason(recommendDTO.getReason());
        recommend.setScore(recommendDTO.getScore());
        recommendRepository.save(recommend);
        logger.info("成功為用戶 ID: {} 新增推薦，店家ID: {}。", managedUser.getId(), store.getId());
    }

    // 獲取用戶的所有推薦
    @Transactional(readOnly = true)
    public List<RecommendDTO> getRecommendsByUserId(Integer userId) {
        UserModel managedUser = userRepository.findById(userId).orElseThrow(() -> {
            logger.error("獲取推薦失敗：用戶 ID {} 不存在。", userId);
            return new EntityNotFoundException("用戶不存在。");
        });
        return recommendRepository.findByUserId(managedUser.getId()).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    // 精確獲取用戶對店家的推薦，用於所有推薦詳情頁面
    @Transactional(readOnly = true)
    public Optional<RecommendDTO> getRecommendByUserAndStoreId(Integer userId, Integer storeId) {
        UserModel user = userRepository.findById(userId).orElseThrow(() -> {
            logger.error("查詢推薦失敗：用戶 ID {} 不存在。", userId);
            return new EntityNotFoundException("用戶不存在。");
        });

        Optional<StoreModel> storeOptional = storeRepository.findById(storeId);
        if (storeOptional.isEmpty()) {
            logger.warn("查詢推薦失敗：店家 ID {} 不存在。", storeId);
            return Optional.empty();
        }
        Optional<RecommendModel> recommendModelOptional = recommendRepository.findByUserIdAndStoreId(user.getId(), storeId);
        return recommendModelOptional.map(this::convertToDto);

    }

    // 更新推薦
    @Transactional
    public void updateRecommend(RecommendDTO recommendDTO, UserModel currentUserFromSession) {
        UserModel managedUser = userRepository.findById(currentUserFromSession.getId()).orElseThrow(() -> {
            logger.error("更新推薦失敗：用戶 ID {} 不存在或會話已過期。", currentUserFromSession.getId());
            return new EntityNotFoundException("用戶不存在或會話已過期。");
        });
        logger.info("嘗試為用戶 ID: {} (Email: {}) 更新推薦。", managedUser.getId(), managedUser.getEmail());
        StoreModel storeToUpdate = storeRepository.findById(recommendDTO.getStoreId()).orElseThrow(() -> {
            logger.error("更新推薦失敗：要更新的店家不存在，店家ID: {}.", recommendDTO.getStoreId());
            return new EntityNotFoundException("要更新的店家不存在。店家ID: " + recommendDTO.getStoreId());
        });
        RecommendModel existingRecommend = recommendRepository.findByUserIdAndStoreId(managedUser.getId(), storeToUpdate.getId()).orElseThrow(() -> {
            logger.error("更新推薦失敗：找不到用戶 ID {} 對店家 ID {} 的推薦或您無權編輯。", managedUser.getId(), recommendDTO.getStoreId());
            return new EntityNotFoundException("找不到該推薦或您無權編輯。用戶ID: " + managedUser.getId() + ", 店家ID: " + recommendDTO.getStoreId());
        });

        storeToUpdate.setName(recommendDTO.getStoreName());
        storeToUpdate.setAddress(recommendDTO.getStoreAddress());
        if (recommendDTO.getStorePhotoUrl() != null && !recommendDTO.getStorePhotoUrl().isEmpty()) {
            storeToUpdate.setPhotoUrl(recommendDTO.getStorePhotoUrl());
        }
        storeRepository.save(storeToUpdate);

        existingRecommend.setReason(recommendDTO.getReason());
        existingRecommend.setScore(recommendDTO.getScore());

        recommendRepository.save(existingRecommend);
        logger.info("成功更新用戶 ID: {} 對店家 ID: {} 的推薦。", managedUser.getId(), recommendDTO.getStoreId());
    }

    // 刪除推薦
    @Transactional
    public void deleteRecommend(Integer userId, Integer storeId, UserModel currentUserFromSession) {
        UserModel managedUser = userRepository.findById(currentUserFromSession.getId()).orElseThrow(() -> {
            logger.error("刪除推薦失敗：用戶 ID {} 不存在或會話已過期。", currentUserFromSession.getId());
            return new EntityNotFoundException("用戶不存在或會話已過期。");
        });

        RecommendModel recommendToDelete = recommendRepository.findByUserIdAndStoreId(managedUser.getId(), storeId).orElseThrow(() -> {
            logger.error("刪除推薦失敗：找不到用戶 ID {} 對店家 ID {} 的推薦或您無權刪除。", managedUser.getId(), storeId);
            return new EntityNotFoundException("找不到該推薦或您無權刪除。");
        });

        recommendRepository.delete(recommendToDelete);
        logger.info("用戶 ID: {} 刪除了對店家 ID: {} 的推薦。", managedUser.getId(), storeId);
    }

    // 網紅最新推薦
    @Transactional(readOnly = true)
    public List<RecommendDTO> findLatestFamousUserRecommends(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return recommendRepository.findLatestFamousUserRecommends(pageable)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    // 所有推薦
    public Page<RecommendDTO> findAllRecommendsPaged(int page, int size, Boolean isFamousFilter, String searchTerm) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RecommendModel> recommendModelPage = recommendRepository.findFilteredRecommends(isFamousFilter,searchTerm,pageable);
        return recommendModelPage.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<RecommendDTO> findRandom6Recommends() {
        return recommendRepository.findRandom6Recommends().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    // 將 RecommendModel 轉換為 RecommendDTO
    private RecommendDTO convertToDto(RecommendModel recommendModel) {
        RecommendDTO dto = new RecommendDTO();

        if (recommendModel.getUser() != null) {
            dto.setUserId(recommendModel.getUser().getId());
            dto.setUserName(recommendModel.getUser().getUserName());
            dto.setFamous(recommendModel.getUser().getIsFamous());
        } else {
            dto.setUserId(null);
            dto.setUserName("未知用戶");
            dto.setFamous(false);
        }

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
        dto.setReason(recommendModel.getReason());
        dto.setScore(recommendModel.getScore());
        dto.setCreatedAt(recommendModel.getCreatedAt());
        return dto;
    }
}