package com.example.topfoodnow.controller;

import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.model.InfluencerModel;
import com.example.topfoodnow.service.StoreService;
import com.example.topfoodnow.service.InfluencerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class IndexController {
    @Autowired
    private StoreService storeService;

    @Autowired
    private InfluencerService influencerService;

    @GetMapping("/")
    public String index(Model model) {
        List<StoreModel> storeList = storeService.findRandom6Stores();
        model.addAttribute("storeList", storeList);

        List<InfluencerModel> influencerList = influencerService.getInfluencers();
        model.addAttribute("influencerList", influencerList);
        return "index";
    }
}
