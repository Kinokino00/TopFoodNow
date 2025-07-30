package com.example.topfoodnow.service;

import com.example.topfoodnow.dto.StoreWithAvgScoreDTO;
import com.example.topfoodnow.model.StoreModel;

import java.util.List;

public interface StoreService {
    List<StoreWithAvgScoreDTO> getRandomStoresWithAverageScore(int limit);
}