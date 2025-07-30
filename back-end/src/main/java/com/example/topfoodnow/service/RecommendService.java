package com.example.topfoodnow.service;

import com.example.topfoodnow.dto.RecommendCreateRequestDTO;
import com.example.topfoodnow.dto.RecommendRequestDTO;
import com.example.topfoodnow.dto.RecommendResponseDTO;
import com.example.topfoodnow.dto.StoreWithAvgScoreDTO;
import com.example.topfoodnow.model.UserModel;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;
import java.io.IOException;

public interface RecommendService {
    RecommendResponseDTO addRecommend(
            RecommendCreateRequestDTO requestDTO,
            List<String> photoUrls,
            UserModel currentUserModel);

    RecommendResponseDTO updateRecommend(
            Integer recommendId,
            RecommendRequestDTO recommendRequestDTO,
            List<String> newUploadedPhotoUrls,
            List<String> retainedPhotoUrls,
            UserModel currentUserModel
    ) throws IOException;

    void deleteRecommend(
            Integer userId,
            Integer storeId,
            UserModel currentUserModel
    ) throws IOException;

    List<RecommendResponseDTO> getRecommendsByUserId(Integer userId);

    Optional<RecommendResponseDTO> getRecommendByUserAndStoreId(Integer userId, Integer storeId);

    Page<RecommendResponseDTO> findAllRecommendsPaged(int page, int size, String[] sort);

    List<RecommendResponseDTO> getRecommendsByStoreId(Integer storeId);
}