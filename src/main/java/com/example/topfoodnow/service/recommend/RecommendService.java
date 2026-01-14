package com.example.topfoodnow.service.recommend;

import com.example.topfoodnow.controller.recommend.dto.Recommend;
import com.example.topfoodnow.model.UserModel;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;

public interface RecommendService {
    void addRecommend(Recommend Recommend, UserModel currentUserFromSession);
    List<Recommend> getRecommendsByUserId(Integer userId);
    Optional<Recommend> getRecommendByUserAndStoreId(Integer userId, Integer storeId);
    void updateRecommend(Recommend Recommend, UserModel currentUserFromSession);
    void deleteRecommend(Integer userId, Integer storeId, UserModel currentUserFromSession);
    List<Recommend> findLatestFamousUserRecommends(int limit);
    Page<Recommend> findAllRecommendsPaged(int page, int size, Boolean isFamousFilter, String searchTerm);
    List<Recommend> findRandom6Recommends();
}

