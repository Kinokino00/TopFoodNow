package com.example.topfoodnow.payload.response;

import com.example.topfoodnow.model.StoreModel;
import lombok.Data;

@Data
public class StoreResponse {
    private Integer id;
    private String name;
    private String address;

    public StoreResponse(StoreModel store) {
        this.id = store.getId();
        this.name = store.getName();
        this.address = store.getAddress();
    }
}