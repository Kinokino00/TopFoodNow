package com.example.topfoodnow.controller.store;

import com.example.topfoodnow.controller.recommend.dto.Recommend;
import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.service.store.StoreService;
import com.example.topfoodnow.service.recommend.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class IndexController {
    @Autowired
    private RecommendService recommendService;

    @GetMapping("/")
    public String index(Model model) {
        List<Recommend> randomRecommends = recommendService.findRandom6Recommends();
        model.addAttribute("randomRecommends", randomRecommends);

        List<Recommend> famousUserNewStoreList = recommendService.findLatestFamousUserRecommends(6);
        model.addAttribute("newStoreList", famousUserNewStoreList);
        return "index";
    }
}

