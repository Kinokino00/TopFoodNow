package com.example.topfoodnow.repository;

import com.example.topfoodnow.model.RecommendModel;
import com.example.topfoodnow.model.RecommendId;
import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.model.StoreModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecommendRepository extends JpaRepository<RecommendModel, RecommendId> {
    List<RecommendModel> findByUserId(Integer userId);

    Optional<RecommendModel> findByUserIdAndStoreId(Integer userId, Integer storeId);

    // 獲取最新且由名人推薦的店家
    @Query("SELECT r FROM RecommendModel r " +
        "JOIN FETCH r.user u " +
        "JOIN FETCH r.store s " +
        "WHERE u.isFamous = true " +
        "ORDER BY r.createdAt DESC")
    List<RecommendModel> findLatestFamousUserRecommends(Pageable pageable);
}