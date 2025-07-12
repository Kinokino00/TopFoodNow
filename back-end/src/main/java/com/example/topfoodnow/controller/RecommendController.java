package com.example.topfoodnow.controller;

import com.example.topfoodnow.dto.RecommendDTO;
import com.example.topfoodnow.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.Principal;
import java.util.Optional;

@Controller
public class RecommendController {
    private static final Logger logger = LoggerFactory.getLogger(RecommendController.class);

    @Autowired
    private RecommendService recommendService;

    @GetMapping("/personal-recommend/{userId}/{storeId}")
    public String viewPersonalRecommend(@PathVariable Integer userId,
                                        @PathVariable Integer storeId,
                                        Model model,
                                        Principal principal,
                                        RedirectAttributes redirectAttributes
    ) {
        logger.info("嘗試查看用戶 ID: {} 對店家 ID: {} 的推薦詳情。", userId, storeId);

        Optional<RecommendDTO> recommendOptional = recommendService.getRecommendByUserAndStoreId(userId, storeId);
        if (recommendOptional.isPresent()) {
            RecommendDTO recommend = recommendOptional.get();
            model.addAttribute("recommend", recommend);
            model.addAttribute("pageTitle", recommend.getStoreName() + " 的推薦詳情");

            boolean isCurrentUserAuthor = false;
            if (principal != null) {
            }
            model.addAttribute("isCurrentUserAuthor", isCurrentUserAuthor);
            model.addAttribute("isAuthenticated", principal != null);
            model.addAttribute("showPersonalRecommendBreadcrumb", false);

            logger.info("找到推薦，顯示詳情頁面。");
            return "recommend-detail";
        } else {
            logger.warn("未找到用戶 ID: {} 和店家 ID: {} 的推薦。重定向回所有推薦頁面。", userId, storeId);
            redirectAttributes.addFlashAttribute("errorMessage", "找不到該推薦的詳細資訊。");
            return "redirect:/all-recommends";
        }
    }
}