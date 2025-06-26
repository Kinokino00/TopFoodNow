package com.example.topfoodnow.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InfluencerModel {
    private int id;
    private String name;
    private String photoUrl;
    private String address;
    private double rating;

    private InfluencerModel influencerModel;

    public InfluencerModel(int id, String name, String photoUrl, String address, double rating) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.address = address;
        this.rating = rating;
    }
}

