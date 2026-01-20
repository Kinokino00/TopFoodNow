package com.example.topfoodnow.infra.storecategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreCategoryRepository extends JpaRepository<StoreCategory, StoreCategoryPK> {
    // 根據 storeId 查找所有分類
    List<StoreCategory> findById_StoreId(Integer storeId);

    // 根據 categoryId 查找所有店家
    List<StoreCategory> findById_CategoryId(Integer categoryId);

    // 檢查特定關聯是否存在
    boolean existsById_StoreIdAndId_CategoryId(Integer storeId, Integer categoryId);

    // 更新或刪除單個精確的關聯
    Optional<StoreCategory> findById_StoreIdAndId_CategoryId(Integer storeId, Integer categoryId);

    // 取得所有分類以便在服務層進行排序和組合。只取得直接關聯，不包含來自 recommened 的間接關聯
    @Query("SELECT sc FROM StoreCategoryModel sc JOIN FETCH sc.category JOIN FETCH sc.store WHERE sc.id.storeId = :storeId")
    List<StoreCategory> findStoreCategoriesByStoreIdWithDetails(@Param("storeId") Integer storeId);

    @Query(value = "SELECT c.id, c.name, COUNT(DISTINCT r.user_id) AS user_count " +
            "FROM category c " +
            "JOIN recommend_category rc ON c.id = rc.category_id " +
            "JOIN recommend r ON rc.recommend_id = r.id " +
            "WHERE r.store_id = :storeId " +
            "GROUP BY c.id, c.name " +
            "ORDER BY user_count DESC",
            nativeQuery = true)
    List<Object[]> findCategoriesByRecommendationCountForStore(@Param("storeId") Integer storeId);

    // 刪除特定關聯
    void deleteById_StoreIdAndId_CategoryId(Integer storeId, Integer categoryId);
}