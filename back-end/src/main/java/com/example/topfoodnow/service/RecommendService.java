package com.example.topfoodnow.service;

import com.example.topfoodnow.dto.RecommendCreateRequestDTO;
import com.example.topfoodnow.dto.RecommendRequestDTO;
import com.example.topfoodnow.dto.RecommendResponseDTO;
import com.example.topfoodnow.model.UserModel;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;

public interface RecommendService {
    RecommendResponseDTO addRecommend(RecommendCreateRequestDTO requestDTO, List<String> photoUrls, UserModel currentUserModel);

    RecommendResponseDTO updateRecommend(RecommendRequestDTO recommendRequestDTO, List<String> photoUrls, UserModel currentUserModel);

    void deleteRecommend(Integer userId, Integer storeId, UserModel currentUserModel);

    List<RecommendResponseDTO> getRecommendsByUserId(Integer userId);

    Optional<RecommendResponseDTO> getRecommendByUserAndStoreId(Integer userId, Integer storeId);

    Page<RecommendResponseDTO> findAllRecommendsPaged(int page, int size, String[] sort);
}