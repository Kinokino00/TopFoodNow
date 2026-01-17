package com.example.topfoodnow.service.store.impl;

import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.repository.StoreRepository;
import com.example.topfoodnow.service.store.StoreService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;

    public List<StoreModel> getAllStores() {
        return storeRepository.findAll();
    }

    public List<StoreModel> findRandom6Stores() {
        return storeRepository.findRandom6Stores();
    }

    public List<StoreModel> findTop6ByOrderByCreatedAtDesc() {
        return storeRepository.findTop6ByOrderByCreatedAtDesc();
    }

    public Optional<StoreModel> getStoreById(Integer id) {
        return storeRepository.findById(id);
    }

    public Optional<StoreModel> findStoreByName(String name) {
        return storeRepository.findByName(name);
    }

    public StoreModel saveStore(StoreModel store) {
        return storeRepository.save(store);
    }

    public void updateStorePhoto(Integer storeId, String photoUrl) {
        storeRepository.findById(storeId).ifPresent(store -> {
            store.setPhotoUrl(photoUrl);
            storeRepository.save(store);
        });
    }
}
