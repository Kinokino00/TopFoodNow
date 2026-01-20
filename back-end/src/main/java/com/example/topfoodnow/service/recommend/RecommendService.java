package com.example.topfoodnow.service.recommend;

import com.example.topfoodnow.controller.recommend.request.RecommendRequest;
import com.example.topfoodnow.controller.recommend.response.RecommendResponse;
import com.example.topfoodnow.controller.recommend.request.CreateRecommendRequest;
import com.example.topfoodnow.infra.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.io.IOException;

public interface RecommendService {
    RecommendResponse addRecommend(
            CreateRecommendRequest requestDTO,
            List<String> photoUrls,
            User currentUser);

    RecommendResponse updateRecommend(
            Integer recommendId,
            RecommendRequest recommendRequest,
            List<String> newUploadedPhotoUrls,
            List<String> retainedPhotoUrls,
            User currentUser
    ) throws IOException;

    void deleteRecommend(
            Integer userId,
            Integer storeId,
            User currentUser
    ) throws IOException;

    List<RecommendResponse> getRecommendsByUserId(Integer userId);

    Optional<RecommendResponse> getRecommendByUserAndStoreId(Integer userId, Integer storeId);

    Page<RecommendResponse> findAllRecommendsPaged(Pageable pageable, String searchTerm);

    List<RecommendResponse> getRecommendsByStoreId(Integer storeId);
}