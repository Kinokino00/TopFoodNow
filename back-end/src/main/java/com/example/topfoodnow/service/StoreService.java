package com.example.topfoodnow.service;

import com.example.topfoodnow.dto.StoreDetailDTO;
import com.example.topfoodnow.dto.StoreWithAvgScoreDTO;
import java.util.List;

public interface StoreService {
    List<StoreWithAvgScoreDTO> getRandomStoresWithAverageScore(int limit);
    StoreDetailDTO getStoreDetails(Integer storeId);
}