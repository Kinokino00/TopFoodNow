package com.example.topfoodnow.service.impl;

import com.example.topfoodnow.dto.StoreWithAvgScoreDTO;
import com.example.topfoodnow.service.StoreService;
import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.repository.StoreRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;

    @Override
    public List<StoreWithAvgScoreDTO> getRandomStoresWithAverageScore(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return storeRepository.findRandomStoresWithAverageScore(pageRequest);
    }
}