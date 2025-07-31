package com.example.topfoodnow.repository;

import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.dto.StoreWithAvgScoreDTO;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<StoreModel, Integer> {
    Optional<StoreModel> findByName(String name);

    boolean existsById(Integer id);

    @Query(value = "SELECT new com.example.topfoodnow.dto.StoreWithAvgScoreDTO(s.id, s.name, CAST(AVG(r.score) AS int)) " + // <-- 這裡改為 CAST(AVG(r.score) AS int)
            "FROM StoreModel s JOIN RecommendModel r ON s.id = r.store.id " +
            "GROUP BY s.id, s.name " +
            "ORDER BY FUNCTION('RAND')")
    List<StoreWithAvgScoreDTO> findRandomStoresWithAverageScore(Pageable pageable);

    @Query(value = "SELECT s FROM StoreModel s ORDER BY FUNCTION('RAND')")
    List<StoreModel> findRandomStores(Pageable pageable);
}