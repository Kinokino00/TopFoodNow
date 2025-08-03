package com.example.topfoodnow.repository;

import com.example.topfoodnow.model.RecommendModel;
import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.dto.CategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecommendRepository extends JpaRepository<RecommendModel, Integer> {
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

    // 用於精確取得用戶對店家的推薦，用於所有推薦詳情頁面
    @Query("SELECT r FROM RecommendModel r " +
            "JOIN FETCH r.user u " +
            "JOIN FETCH r.store s " +
            "LEFT JOIN FETCH r.categories c " +
            "WHERE u.id = :userId AND s.id = :storeId")
    Optional<RecommendModel> findByUserAndStoreIdWithUserAndStoreAndCategories(
        @Param("userId") Integer userId,
        @Param("storeId") Integer storeId
    );

    // 所有推薦分頁
    @Query("SELECT r FROM RecommendModel r " +
            "JOIN FETCH r.user u " +
            "JOIN FETCH r.store s " +
            "LEFT JOIN FETCH r.categories c " + // 確保即使沒有分類也能查詢
            "WHERE (:searchTerm IS NULL OR :searchTerm = '' OR " +
            "       LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "       LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "       LOWER(s.address) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "       LOWER(r.reason) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "       EXISTS (SELECT cat FROM r.categories cat WHERE cat.id = c.id AND LOWER(cat.categoryName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))" +
            ")")
    Page<RecommendModel> findFilteredRecommendsWithUserAndStoreAndCategories(
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    // 取隨機 3 筆推薦 (此方法返回 RecommendModel，如果需要特定數據才用它)
    @Query(value = "SELECT r.* FROM recommend r " +
            "JOIN user u ON r.user_id = u.id " +
            "JOIN store s ON r.store_id = s.id " +
            "ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<RecommendModel> findRandom3RecommendsWithUserAndStoreAndCategories();

    // 取得特定店家按用戶選擇次數排序的分類列表
    @Query(value = "SELECT new com.example.topfoodnow.dto.CategoryDTO(cat.id, cat.categoryName ) " +
            "FROM RecommendModel r " +
            "JOIN r.categories cat " +
            "WHERE r.store.id = :storeId " +
            "GROUP BY cat.id, cat.categoryName  " +
            "ORDER BY COUNT(DISTINCT r.user.id) DESC, cat.id ASC")
    List<CategoryDTO> findCategoriesByStorePopularity(@Param("storeId") Integer storeId);
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