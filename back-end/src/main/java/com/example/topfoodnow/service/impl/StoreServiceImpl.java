package com.example.topfoodnow.service.impl;

import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.model.RecommendModel;
import com.example.topfoodnow.model.CategoryModel;
import com.example.topfoodnow.service.StoreService;
import com.example.topfoodnow.dto.StoreDetailDTO;
import com.example.topfoodnow.dto.StoreWithAvgScoreDTO;
import com.example.topfoodnow.repository.StoreRepository;
import com.example.topfoodnow.repository.RecommendRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import java.util.Set;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {
    private final RecommendRepository recommendRepository;
    private final StoreRepository storeRepository;

    public List<StoreWithAvgScoreDTO> getRandomStoresWithAverageScore(int limit) {
        List<RecommendModel> randomRecommends = recommendRepository.findRandomRecommends(limit);

        return randomRecommends.stream().map(recommend -> {
            Integer recommendId = recommend.getId();
            Integer userId = recommend.getUser().getId();
            Integer storeId = recommend.getStore().getId();
            String storeName = recommend.getStore().getName();

            Double averageScore = recommendRepository.findAverageScoreByStoreId(storeId);
            Integer finalAverageScore = (averageScore != null) ? (int) Math.round(averageScore) : 0;
            String latestPhotoUrl = getLatestPhotoUrl(storeId);

            return new StoreWithAvgScoreDTO(
                    recommendId,
                    userId,
                    storeId,
                    storeName,
                    finalAverageScore,
                    latestPhotoUrl
            );
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StoreDetailDTO getStoreDetails(Integer storeId) {
        StoreModel store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found with id: " + storeId));

        Double averageScore = recommendRepository.findAverageScoreByStoreId(storeId);
        Integer finalAverageScore = (averageScore != null) ? (int) Math.round(averageScore) : 0;

        List<RecommendModel> recommends = recommendRepository.findByStoreId(storeId);
        Set<String> categoryNames = recommends.stream()
                .flatMap(recommend -> recommend.getCategories().stream())
                .map(CategoryModel::getCategoryName)
                .collect(Collectors.toSet());

        return new StoreDetailDTO(
                storeId,
                store.getName(),
                store.getAddress(),
                finalAverageScore,
                categoryNames.stream().collect(Collectors.toList())
        );
    }

    private String getLatestPhotoUrl(Integer storeId) {
        String latestPhotoUrl = null;
        Optional<RecommendModel> latestRecommend = recommendRepository.findTopByStoreIdOrderByCreatedAtDesc(storeId);

        if (latestRecommend.isPresent()) {
            List<String> photoUrls = latestRecommend.get().getPhotoUrls();
            if (photoUrls != null && !photoUrls.isEmpty()) {
                latestPhotoUrl = photoUrls.get(0);
            }
        }
        return latestPhotoUrl;
    }
}