 package com.example.topfoodnow.repository;

import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.dto.StoreWithAvgScoreDTO;
import com.example.topfoodnow.dto.StoreWithAvgScoreDTO;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<StoreModel, Integer> {
    Optional<StoreModel> findByName(String name);

    // 根據ID判斷店家是否存在
    boolean existsById(Integer id);

    // 隨機獲取店家及其平均評分
    @Query(value = "SELECT new com.example.topfoodnow.dto.StoreWithAvgScoreDTO(s.id, s.name, AVG(r.score)) " +
            "FROM StoreModel s JOIN RecommendModel r ON s.id = r.store.id " +
            "GROUP BY s.id, s.name " +
            "ORDER BY FUNCTION('RAND')")
    List<StoreWithAvgScoreDTO> findRandomStoresWithAverageScore(Pageable pageable);

}
