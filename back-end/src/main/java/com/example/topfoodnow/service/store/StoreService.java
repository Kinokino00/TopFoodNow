package com.example.topfoodnow.service.store;

import com.example.topfoodnow.controller.store.response.StoreDetailResponse;
import com.example.topfoodnow.controller.store.response.StoreWithAvgScoreResponse;
import java.util.List;

public interface StoreService {
    List<StoreWithAvgScoreResponse> getRandomStoresWithAverageScore(int limit);
    StoreDetailResponse getStoreDetails(Integer storeId);
}