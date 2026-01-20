package com.example.topfoodnow.service.store.impl;

import com.example.topfoodnow.infra.store.Store;
import com.example.topfoodnow.infra.recommend.Recommend;
import com.example.topfoodnow.infra.category.Category;
import com.example.topfoodnow.controller.store.response.StoreDetailResponse;
import com.example.topfoodnow.controller.store.response.StoreWithAvgScoreResponse;
import com.example.topfoodnow.infra.store.StoreRepository;
import com.example.topfoodnow.infra.recommend.RecommendRepository;
import com.example.topfoodnow.service.store.StoreService;
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

    public List<StoreWithAvgScoreResponse> getRandomStoresWithAverageScore(int limit) {
        List<Recommend> randomRecommends = recommendRepository.findRandomRecommends(limit);

        return randomRecommends.stream().map(recommend -> {
            Integer recommendId = recommend.getId();
            Integer userId = recommend.getUser().getId();
            Integer storeId = recommend.getStore().getId();
            String storeName = recommend.getStore().getName();

            Double averageScore = recommendRepository.findAverageScoreByStoreId(storeId);
            Integer finalAverageScore = (averageScore != null) ? (int) Math.round(averageScore) : 0;
            String latestPhotoUrl = getLatestPhotoUrl(storeId);

            return new StoreWithAvgScoreResponse(
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
    public StoreDetailResponse getStoreDetails(Integer storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found with id: " + storeId));

        Double averageScore = recommendRepository.findAverageScoreByStoreId(storeId);
        Integer finalAverageScore = (averageScore != null) ? (int) Math.round(averageScore) : 0;

        List<Recommend> recommends = recommendRepository.findByStoreId(storeId);
        Set<String> categoryNames = recommends.stream()
                .flatMap(recommend -> recommend.getCategories().stream())
                .map(Category::getCategoryName)
                .collect(Collectors.toSet());

        return new StoreDetailResponse(
                storeId,
                store.getName(),
                store.getAddress(),
                finalAverageScore,
                categoryNames.stream().collect(Collectors.toList())
        );
    }

    private String getLatestPhotoUrl(Integer storeId) {
        String latestPhotoUrl = null;
        Optional<Recommend> latestRecommend = recommendRepository.findTopByStoreIdOrderByCreatedAtDesc(storeId);

        if (latestRecommend.isPresent()) {
            List<String> photoUrls = latestRecommend.get().getPhotoUrls();
            if (photoUrls != null && !photoUrls.isEmpty()) {
                latestPhotoUrl = photoUrls.get(0);
            }
        }
        return latestPhotoUrl;
    }
}