package com.example.topfoodnow.repository;

import com.example.topfoodnow.model.StoreModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<StoreModel, Integer> {
    Optional<StoreModel> findByName(String name);

    boolean existsById(Integer id);
}