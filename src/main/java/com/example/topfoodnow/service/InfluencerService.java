package com.example.topfoodnow.service;

import com.example.topfoodnow.model.InfluencerModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InfluencerService {
    public List<InfluencerModel> getInfluencers() {
        return List.of(
            new InfluencerModel(1, "網紅1", "https://example.com/photo1", "地址1", 4.5),
            new InfluencerModel(2, "網紅2", "https://example.com/photo2", "地址2", 4.0)
        );
    }
}

