package com.example.topfoodnow.repository;

import com.example.topfoodnow.model.RecommendModel;
import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.dto.CategoryDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecommendRepository extends JpaRepository<RecommendModel, Integer>, JpaSpecificationExecutor<RecommendModel> {
    // 根據用戶和店家查找推薦
    @Query("SELECT r FROM RecommendModel r " +
            "JOIN FETCH r.user u " +
            "JOIN FETCH r.store s " +
            "LEFT JOIN FETCH r.categories c " +
            "WHERE u = :user AND s = :store")
    Optional<RecommendModel> findByUserAndStore(
            @Param("user") UserModel user,
            @Param("store") StoreModel store
    );

    // 根據用戶ID查找所有推薦
    @Query("SELECT r FROM RecommendModel r " +
            "JOIN FETCH r.user u " +
            "JOIN FETCH r.store s " +
            "LEFT JOIN FETCH r.categories c " +
            "WHERE u.id = :userId")
    List<RecommendModel> findByUserId(@Param("userId") Integer userId);

    // 根據用戶ID和店家ID查找推薦 (JOIN FETCH，避免 N+1 問題)
    @Query("SELECT r FROM RecommendModel r " +
            "JOIN FETCH r.user u " +
            "JOIN FETCH r.store s " +
            "LEFT JOIN FETCH r.categories c " +
            "WHERE u.id = :userId AND s.id = :storeId")
    Optional<RecommendModel> findByUserIdAndStoreId(
            @Param("userId") Integer userId,
            @Param("storeId") Integer storeId
    );

    @Query("SELECT r FROM RecommendModel r " +
            "JOIN FETCH r.user u " +
            "JOIN FETCH r.store s " +
            "ORDER BY FUNCTION('RAND') LIMIT :limit")
    List<RecommendModel> findRandomRecommends(@Param("limit") int limit);

    // 取隨機 3 筆推薦 (此方法返回 RecommendModel，如果需要特定數據才用它)
    @Query(value = "SELECT r.* FROM recommend r " +
            "JOIN user u ON r.user_id = u.id " +
            "JOIN store s ON r.store_id = s.id " +
            "ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<RecommendModel> findRandom3RecommendsWithUserAndStoreAndCategories();

    // 計算與特定店家關聯的推薦數量
    int countByStoreId(Integer storeId);

    // 取得店家ID
    List<RecommendModel> findByStoreId(Integer storeId);

    // 查詢某店家的平均評分 - 返回 Double，讓 Service 層處理四捨五入
    @Query("SELECT CAST(AVG(r.score) AS int) " +
            "FROM RecommendModel r " +
            "WHERE r.store.id = :storeId")
    Double findAverageScoreByStoreId(@Param("storeId") Integer storeId);

    // 查詢某店家最新的推薦
    Optional<RecommendModel> findTopByStoreIdOrderByCreatedAtDesc(Integer storeId);
}