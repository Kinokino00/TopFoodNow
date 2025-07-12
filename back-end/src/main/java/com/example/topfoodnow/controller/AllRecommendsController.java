package com.example.topfoodnow.controller;

import com.example.topfoodnow.dto.RecommendDTO;
import com.example.topfoodnow.service.RecommendService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Arrays;

@Controller
public class AllRecommendsController {
    @Autowired
    private RecommendService recommendService;

    @GetMapping("/all-recommends")
    public String allRecommends(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean isFamous,
            @RequestParam(required = false) String search,
            Model model
    ) {
        Boolean isFamousFilter = isFamous;

        Page<RecommendDTO> allRecommendsPage = recommendService.findAllRecommendsPaged(page, size, isFamousFilter, search);

        model.addAttribute("allRecommendsPage", allRecommendsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("isFamousFilter", isFamousFilter);
        model.addAttribute("searchTerm", search);

        model.addAttribute("pageSizes", Arrays.asList(10, 25, 50));

        return "all-recommends";
    }
}