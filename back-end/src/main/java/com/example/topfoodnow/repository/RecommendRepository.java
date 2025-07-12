package com.example.topfoodnow.repository;

import com.example.topfoodnow.model.RecommendModel;
import com.example.topfoodnow.model.RecommendId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT r FROM RecommendModel r " +
            "JOIN FETCH r.user u " +
            "JOIN FETCH r.store s " +
            "WHERE (:isFamousFilter IS NULL OR u.isFamous = :isFamousFilter) " +
            "AND (:searchTerm IS NULL OR :searchTerm = '' " +
            "       OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "       OR LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "       OR LOWER(s.address) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY r.createdAt DESC")
    Page<RecommendModel> findFilteredRecommends(
            @Param("isFamousFilter") Boolean isFamousFilter,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    @Query(value = "SELECT * FROM recommend_user ORDER BY RAND() LIMIT 6", nativeQuery = true)
    List<RecommendModel> findRandom6Recommends();
}