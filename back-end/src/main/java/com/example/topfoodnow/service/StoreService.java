package com.example.topfoodnow.service;

import com.example.topfoodnow.dto.StoreWithAvgScoreDTO;
import java.util.List;

public interface StoreService {
    public List<StoreWithAvgScoreDTO> getRandomStoresWithAverageScore(int limit);
}