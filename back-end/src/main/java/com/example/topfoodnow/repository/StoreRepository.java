package com.example.topfoodnow.repository;

import com.example.topfoodnow.model.StoreModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<StoreModel, Integer> {
    @Query(value = "SELECT * FROM store ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<StoreModel> findRandom3Stores();

    List<StoreModel> findTop6ByOrderByCreatedAtDesc();

    Optional<StoreModel> findByName(String name);
}
