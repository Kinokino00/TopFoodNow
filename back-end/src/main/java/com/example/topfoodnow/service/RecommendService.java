package com.example.topfoodnow.service;

import com.example.topfoodnow.dto.RecommendDTO;
import com.example.topfoodnow.model.UserModel;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;

public interface RecommendService {
    void addRecommend(RecommendDTO recommendDTO, UserModel currentUserFromSession);
    List<RecommendDTO> getRecommendsByUserId(Integer userId);
    Optional<RecommendDTO> getRecommendByUserAndStoreId(Integer userId, Integer storeId);
    void updateRecommend(RecommendDTO recommendDTO, UserModel currentUserFromSession);
    void deleteRecommend(Integer userId, Integer storeId, UserModel currentUserFromSession);
    List<RecommendDTO> findLatestFamousUserRecommends(int limit);
    Page<RecommendDTO> findAllRecommendsPaged(int page, int size, Boolean isFamousFilter, String searchTerm);
    List<RecommendDTO> findRandom6Recommends();
}