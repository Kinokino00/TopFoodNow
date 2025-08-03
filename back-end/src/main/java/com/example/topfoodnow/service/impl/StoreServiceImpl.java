package com.example.topfoodnow.service.impl;

import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.model.RecommendModel;
import com.example.topfoodnow.service.StoreService;
import com.example.topfoodnow.dto.StoreWithAvgScoreDTO;
import com.example.topfoodnow.repository.StoreRepository;
import com.example.topfoodnow.repository.RecommendRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final RecommendRepository recommendRepository;

    public List<StoreWithAvgScoreDTO> getRandomStoresWithAverageScore(int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        List<StoreModel> randomStores = storeRepository.findRandomStores(pageable);

        return randomStores.stream().map(store -> {
            // 從 repository 獲取原始的 Double 平均分數
            Double averageScore = recommendRepository.findAverageScoreByStoreId(store.getId());

            // 處理平均分數：如果為 null 則設為 0，然後四捨五入取整為 Integer
            Integer finalAverageScore = (averageScore != null) ? (int) Math.round(averageScore) : 0;
            // Math.round(double) 返回 long，需要強制轉型為 int

            String latestPhotoUrl = null;
            Optional<RecommendModel> latestRecommend = recommendRepository.findTopByStoreIdOrderByCreatedAtDesc(store.getId());

            if (latestRecommend.isPresent()) {
                List<String> photoUrls = latestRecommend.get().getPhotoUrls();
                if (photoUrls != null && !photoUrls.isEmpty()) {
                    latestPhotoUrl = photoUrls.get(0);
                }
            }

            return new StoreWithAvgScoreDTO(store.getId(), store.getName(), finalAverageScore, latestPhotoUrl);
        }).collect(Collectors.toList());
    }
}