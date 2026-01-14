package com.example.topfoodnow.controller.recommend;

import com.example.topfoodnow.controller.recommend.dto.Recommend;
import com.example.topfoodnow.service.recommend.RecommendService;
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
        logger.info("?óË©¶?•Á??®Êà∂ ID: {} Â∞çÂ?ÂÆ?ID: {} ?ÑÊé®?¶Ë©≥?Ö„Ä?, userId, storeId);

        Optional<Recommend> recommendOptional = recommendService.getRecommendByUserAndStoreId(userId, storeId);
        if (recommendOptional.isPresent()) {
            Recommend recommend = recommendOptional.get();
            model.addAttribute("recommend", recommend);
            model.addAttribute("pageTitle", recommend.getStoreName() + " ?ÑÊé®?¶Ë©≥??);

            boolean isCurrentUserAuthor = false;
            if (principal != null) {
            }
            model.addAttribute("isCurrentUserAuthor", isCurrentUserAuthor);
            model.addAttribute("isAuthenticated", principal != null);
            model.addAttribute("showPersonalRecommendBreadcrumb", false);

            logger.info("?æÂà∞?®Ëñ¶ÔºåÈ°ØÁ§∫Ë©≥?ÖÈ??¢„Ä?);
            return "recommend-detail";
        } else {
            logger.warn("?™Êâæ?∞Áî®??ID: {} ?åÂ?ÂÆ?ID: {} ?ÑÊé®?¶„ÄÇÈ?ÂÆöÂ??ûÊ??âÊé®?¶È??¢„Ä?, userId, storeId);
            redirectAttributes.addFlashAttribute("errorMessage", "?æ‰??∞Ë©≤?®Ëñ¶?ÑË©≥Á¥∞Ë?Ë®ä„Ä?);
            return "redirect:/all-recommends";
        }
    }
}
