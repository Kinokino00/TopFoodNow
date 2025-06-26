package com.example.topfoodnow.repository;

import com.example.topfoodnow.model.RecommendModel;
import com.example.topfoodnow.model.RecommendId;
import com.example.topfoodnow.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecommendRepository extends JpaRepository<RecommendModel, RecommendId> {
    List<RecommendModel> findByUser(UserModel user);
    Optional<RecommendModel> findByUserIdAndStoreId(Integer userId, Integer storeId);
}