package com.example.topfoodnow.service;

import com.example.topfoodnow.dto.RecommendDTO;
import com.example.topfoodnow.model.RecommendId;
import com.example.topfoodnow.model.RecommendModel;
import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.repository.RecommendRepository;
import com.example.topfoodnow.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendService {
    private final RecommendRepository recommendRepository;
    private final StoreRepository storeRepository;
    private final UserService userService;

    public List<RecommendDTO> getRecommendsByUserId(UserModel user) {
        List<RecommendModel> recommends = recommendRepository.findByUser(user);

        return recommends.stream().map(rec -> {
            RecommendDTO dto = new RecommendDTO();
            dto.setUserId(rec.getUser().getId());
            dto.setStoreId(rec.getStore().getId());
            dto.setUserName(rec.getUser().getUserName());
            dto.setStoreName(rec.getStore().getName());
            dto.setStoreAddress(rec.getStore().getAddress());
            dto.setStorePhotoUrl(rec.getStore().getPhotoUrl());
            dto.setReason(rec.getReason());
            dto.setScore(rec.getScore());
            dto.setCreatedAt(rec.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    // 新增推薦
    public RecommendModel addRecommend(RecommendDTO recommendDTO, UserModel currentUser) {
        StoreModel targetStore;
        Optional<StoreModel> existingStore = storeRepository.findByName(recommendDTO.getStoreName());

        if (existingStore.isPresent()) {
            targetStore = existingStore.get();
        } else {
            StoreModel newStore = new StoreModel();
            newStore.setName(recommendDTO.getStoreName());
            newStore.setAddress(recommendDTO.getStoreAddress());
            newStore.setPhotoUrl(recommendDTO.getStorePhotoUrl());
            targetStore = storeRepository.save(newStore);
        }

        RecommendId id = new RecommendId(currentUser.getId(), targetStore.getId());
        if (recommendRepository.existsById(id)) {
            throw new IllegalArgumentException("您已經推薦過這家餐廳了！");
        }

        RecommendModel recommend = new RecommendModel();
        recommend.setUser(currentUser);
        recommend.setStore(targetStore);

        recommend.setReason(recommendDTO.getReason());
        recommend.setScore(recommendDTO.getScore());

        return recommendRepository.save(recommend);
    }

    // 編輯推薦
    public RecommendModel updateRecommend(RecommendDTO updatedDto, UserModel currentUser) {
        RecommendId idToUpdate = new RecommendId(currentUser.getId(), updatedDto.getStoreId());
        RecommendModel existingRecommend = recommendRepository.findById(idToUpdate).orElseThrow(() -> new EntityNotFoundException("Recommend not found for this user and store."));

        if (existingRecommend.getUser().getId() != currentUser.getId()) {
            throw new SecurityException("Unauthorized access: You can only edit your own recommends.");
        }

        existingRecommend.setReason(updatedDto.getReason());
        existingRecommend.setScore(updatedDto.getScore());

        if (updatedDto.getStorePhotoUrl() != null && !updatedDto.getStorePhotoUrl().isEmpty()) {
            existingRecommend.getStore().setPhotoUrl(updatedDto.getStorePhotoUrl());
            storeRepository.save(existingRecommend.getStore());
        }
        return recommendRepository.save(existingRecommend);
    }

    // 刪除推薦
    public void deleteRecommend(Integer userId, Integer storeId, UserModel currentUser) {
        RecommendId idToDelete = new RecommendId(userId, storeId);
        RecommendModel recommendToDelete = recommendRepository.findById(idToDelete).orElseThrow(() -> new EntityNotFoundException("Recommend not found for user_id " + userId + " and store_id " + storeId));

        if (recommendToDelete.getUser().getId() != currentUser.getId()) {
            throw new SecurityException("Unauthorized access: You can only delete your own recommends.");
        }
        recommendRepository.delete(recommendToDelete);
    }

    // 根據複合主鍵查詢單一推薦
    public Optional<RecommendDTO> getRecommendsByUserIdAndStoreId(Integer userId, Integer storeId) { // Changed method name
        RecommendId id = new RecommendId(userId, storeId);
        return recommendRepository.findById(id).map(rec -> {
            RecommendDTO dto = new RecommendDTO();
            dto.setUserId(rec.getUser().getId());
            dto.setStoreId(rec.getStore().getId());
            dto.setUserName(rec.getUser().getUserName());
            dto.setStoreName(rec.getStore().getName());
            dto.setStoreAddress(rec.getStore().getAddress());
            dto.setStorePhotoUrl(rec.getStore().getPhotoUrl());
            dto.setReason(rec.getReason());
            dto.setScore(rec.getScore());
            dto.setCreatedAt(rec.getCreatedAt());
            return dto;
        });
    }
}