package com.example.topfoodnow.repository;

import com.example.topfoodnow.model.RecommendModel;
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
    // 根據用戶ID和店家ID查找推薦 (JOIN FETCH，避免 N+1 問題)
    @Query("SELECT r FROM RecommendModel r JOIN FETCH r.user u JOIN FETCH r.store s LEFT JOIN FETCH r.categories c WHERE u.id = :userId AND s.id = :storeId")
    Optional<RecommendModel> findByUserIdAndStoreId(@Param("userId") Integer userId, @Param("storeId") Integer storeId);

    // 根據用戶ID查找所有推薦
    @Query("SELECT r FROM RecommendModel r JOIN FETCH r.user u JOIN FETCH r.store s LEFT JOIN FETCH r.categories c WHERE u.id = :userId")
    List<RecommendModel> findByUserWithUserAndStoreAndCategories(@Param("userId") Integer userId);

    // 用於精確取得用戶對店家的推薦，用於所有推薦詳情頁面
    @Query("SELECT r FROM RecommendModel r JOIN FETCH r.user u JOIN FETCH r.store s LEFT JOIN FETCH r.categories c WHERE u.id = :userId AND s.id = :storeId")
    Optional<RecommendModel> findByUserAndStoreIdWithUserAndStoreAndCategories(@Param("userId") Integer userId, @Param("storeId") Integer storeId);

    // 所有推薦分頁
    @Query("SELECT r FROM RecommendModel r JOIN FETCH r.user u JOIN FETCH r.store s LEFT JOIN FETCH r.categories c WHERE " +
        "(:searchTerm IS NULL OR :searchTerm = '' OR s.name LIKE %:searchTerm% OR s.address LIKE %:searchTerm% OR r.reason LIKE %:searchTerm%)")
    Page<RecommendModel> findFilteredRecommendsWithUserAndStoreAndCategories(
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    // 取隨機 3 筆推薦
    @Query(value = "SELECT r.* FROM recommend r " +
            "JOIN user u ON r.user_id = u.id " +
            "JOIN store s ON r.store_id = s.id " +
            "ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<RecommendModel> findRandom3RecommendsWithUserAndStoreAndCategories();


    // 獲取得特定店家按用戶選擇次數排序的分類列表
    @Query(value = "SELECT new com.example.topfoodnow.dto.CategoryDTO(c.id, c.name, COUNT(DISTINCT r.user.id)) " + // 傳入用戶選擇次數
            "FROM RecommendModel r " +
            "JOIN r.categories c " +
            "WHERE r.store.id = :storeId " +
            "GROUP BY c.id, c.name " +
            "ORDER BY COUNT(DISTINCT r.user.id) DESC, c.id ASC",
            countQuery = "SELECT COUNT(DISTINCT c.id) FROM RecommendModel r JOIN r.categories c WHERE r.store.id = :storeId")
    List<CategoryDTO> findCategoriesByStorePopularity(@Param("storeId") Integer storeId);
}