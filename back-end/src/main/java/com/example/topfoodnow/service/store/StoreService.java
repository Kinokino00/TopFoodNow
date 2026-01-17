package com.example.topfoodnow.service.store;

import com.example.topfoodnow.model.StoreModel;
import java.util.List;
import java.util.Optional;

public interface StoreService {
    List<StoreModel> getAllStores();
    Optional<StoreModel> getStoreById(Integer id);
    List<StoreModel> findRandom6Stores();
    List<StoreModel> findTop6ByOrderByCreatedAtDesc();
    Optional<StoreModel> findStoreByName(String name);
    StoreModel saveStore(StoreModel store);
    void updateStorePhoto(Integer storeId, String photoUrl);
}
