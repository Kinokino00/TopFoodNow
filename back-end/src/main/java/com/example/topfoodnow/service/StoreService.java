package com.example.topfoodnow.service;

import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.payload.request.StoreCreateRequest;
import com.example.topfoodnow.payload.request.StoreUpdateRequest;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface StoreService {
    List<StoreModel> findRandom3Stores();

    Optional<StoreModel> getStoreById(Integer storeId);

    StoreModel createStore(StoreCreateRequest request) throws IOException;

    StoreModel updateStore(Integer storeId, StoreUpdateRequest request) throws IOException;

    void deleteStore(Integer storeId) throws IOException;
}