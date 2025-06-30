package com.example.topfoodnow.controller;

import com.example.topfoodnow.dto.RecommendDTO;
import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.service.UserService;
import com.example.topfoodnow.service.MailService;
import com.example.topfoodnow.service.StoreService;
import com.example.topfoodnow.service.RecommendService;
import com.example.topfoodnow.service.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor // 自動生成包含所有 final 欄位的建構子，並注入它們
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${app.base-url}")
    private String appBaseUrl;

    private final UserService userService;
    private final MailService mailService;
    private final RecommendService recommendService;
    private final StoreService storeService;
    private final FileStorageService fileStorageService;

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new UserModel());
        return "register";
    }

    @PostMapping("/register")
    public String registerProcess(@Valid UserModel user,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            logger.warn("註冊表單驗證失敗，錯誤數量: {}", bindingResult.getErrorCount());
            return "register";
        }

        try {
            if (userService.findByEmail(user.getEmail()).isPresent()) {
                logger.warn("註冊嘗試：電子郵件 {} 已被註冊。", user.getEmail());
                model.addAttribute("error", "該電子郵件已被註冊，請使用其他信箱。");
                model.addAttribute("user", user);
                return "register";
            }
            userService.addUser(user);
            logger.info("新用戶 {} 註冊成功，驗證信已發送。", user.getEmail());
            redirectAttributes.addFlashAttribute("email", user.getEmail());
            return "redirect:/register-success";
        } catch (Exception e) {
            logger.error("用戶註冊失敗，Email: {}. 錯誤訊息: {}", user.getEmail(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "註冊失敗，請稍後再試。");
            return "redirect:/register";
        }
    }

    @GetMapping("/register-success")
    public String showRegisterSuccessPage(Model model) {
        model.addAttribute("pageTitle", "註冊成功");
        return "register-success";
    }


    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "emailSent", required = false) String emailSent,
                                Model model) {
        model.addAttribute("pageTitle", "用戶登入");
        if (error != null) {
            logger.warn("登入頁面顯示錯誤訊息：{}", error);
            model.addAttribute("error", "信箱或密碼錯誤，請再試一次。");
        }
        if (logout != null) {
            logger.info("登入頁面顯示登出訊息：{}", logout);
            model.addAttribute("logout", "您已成功登出。");
        }
        if (emailSent != null) {
            logger.info("登入頁面顯示驗證信已發送訊息：{}", emailSent);
            model.addAttribute("emailSent", "驗證信已發送，請檢查您的信箱以啟用帳戶。");
        }
        return "login";
    }

    @PostMapping("/login")
    public String loginProcess(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        model.addAttribute("pageTitle", "用戶登入");

        UserModel authenticatedUser = userService.authenticate(email, password);
        if (authenticatedUser != null) {
            if (!authenticatedUser.isEnabled()) {
                logger.warn("登入嘗試失敗：用戶 {} 帳戶未啟用。", email);
                model.addAttribute("error", "您的帳戶尚未啟用，請檢查您的信箱完成驗證。");
                return "login";
            }
            session.setAttribute("user", authenticatedUser);
            logger.info("用戶 {} 登入成功。", email);
            return "redirect:/";
        } else {
            logger.warn("登入嘗試失敗：Email 或密碼不正確，嘗試登入的 Email: {}", email);
            model.addAttribute("error", "信箱或密碼不正確，請再試一次。");
            return "login";
        }
    }

    // 登出
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        String userEmail = null;
        if (session.getAttribute("user") instanceof UserModel) {
            userEmail = ((UserModel) session.getAttribute("user")).getEmail();
        }
        session.invalidate();
        logger.info("用戶 {} 登出成功。", userEmail != null ? userEmail : "未知用戶");
        return "redirect:/login?logout";
    }

    // 忘記密碼
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        model.addAttribute("pageTitle", "忘記密碼");
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordProcess(@RequestParam("email") String email,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        model.addAttribute("pageTitle", "忘記密碼");

        Optional<UserModel> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.info("忘記密碼請求：Email {} 不存在於系統中。", email);
            model.addAttribute("successMessage", "如果此信箱已註冊，重設密碼連結將會發送到您的信箱。請檢查您的收件箱。");
            return "forgot-password";
        }

        UserModel user = userOptional.get();
        try {
            String token = userService.createPasswordResetTokenForUser(user);
            String resetLink = appBaseUrl +  "/reset-password?token=" + token;
            mailService.sendPasswordResetEmail(user.getEmail(), user.getUserName(), resetLink);
            logger.info("用戶 {} 的密碼重設連結已生成並發送。", user.getEmail());
            model.addAttribute("successMessage", "重設密碼連結已發送，請檢查您的信箱。");
        } catch (Exception e) {
            logger.error("處理忘記密碼請求失敗，Email: {}. 錯誤訊息: {}", email, e.getMessage(), e);
            model.addAttribute("errorMessage", "發送重設連結時發生錯誤，請稍後再試。");
        }
        return "forgot-password";
    }

    // 重設密碼
    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token, Model model) {
        model.addAttribute("pageTitle", "重設密碼");

        Optional<UserModel> userOptional = userService.validatePasswordResetToken(token);
        if (userOptional.isEmpty()) {
            logger.warn("顯示重設密碼頁面失敗：無效或已過期 Token: {}", token);
            model.addAttribute("errorMessage", "無效或已過期的重設密碼連結。請重新申請。");
            return "forgot-password";
        }
        logger.info("顯示重設密碼頁面成功，Token: {}", token);
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPasswordProcess(@RequestParam("token") String token,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        model.addAttribute("pageTitle", "重設密碼");

        if (!newPassword.equals(confirmPassword)) {
            logger.warn("重設密碼失敗：Token {} 的新密碼和確認密碼不一致。", token);
            model.addAttribute("errorMessage", "新密碼和確認密碼不一致。");
            model.addAttribute("token", token);
            return "reset-password";
        }

        if (newPassword.length() < 8) {
            logger.warn("重設密碼失敗：Token {} 的密碼長度不足。", token);
            model.addAttribute("errorMessage", "密碼長度至少為 8 個字符。");
            model.addAttribute("token", token);
            return "reset-password";
        }

        Optional<UserModel> userOptional = userService.validatePasswordResetToken(token);
        if (userOptional.isEmpty()) {
            logger.warn("重設密碼處理失敗：無效或已過期 Token: {}", token);
            redirectAttributes.addFlashAttribute("errorMessage", "無效或已過期的重設密碼連結。請重新申請。");
            return "redirect:/forgot-password";
        }

        UserModel user = userOptional.get();
        userService.changeUserPassword(user, newPassword);
        logger.info("用戶 {} 的密碼已成功重設。", user.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "您的密碼已成功重設，請使用新密碼登入。");
        return "redirect:/login";
    }

    // 個人推薦
    @GetMapping("/personal-recommend")
    public String showPersonalRecommend(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        UserModel currentUser = (UserModel) session.getAttribute("user");
        if (currentUser == null) {
            logger.warn("未登入用戶嘗試訪問個人推薦頁面。");
            return "redirect:/login";
        }
        List<RecommendDTO> userRecommends = recommendService.getRecommendByUserId(currentUser);
        model.addAttribute("userName", currentUser.getUserName());
        model.addAttribute("recommends", userRecommends);
        model.addAttribute("pageTitle", "我的推薦");
        return "personal-recommend";
    }

    @GetMapping("/personal-recommend/add")
    public String showAddRecommendForm(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        model.addAttribute("pageTitle", "新增我的推薦");
        model.addAttribute("recommendDTO", new RecommendDTO());
        return "recommend-form";
    }

    // 處理新增推薦的提交
    @PostMapping("/personal-recommend/add")
    public String addRecommendProcess(@ModelAttribute("recommendDTO") @Validated RecommendDTO recommendDTO,
                                      BindingResult bindingResult,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        UserModel currentUser = (UserModel) session.getAttribute("user");
        if (currentUser == null) {
            logger.warn("未登入用戶嘗試新增推薦。");
            return "redirect:/login";
        }
        logger.info("當前登入用戶 ID: {}", currentUser.getId());
        logger.info("當前登入用戶 Email: {}", currentUser.getEmail());

        if (recommendDTO.getStorePhoto() == null || recommendDTO.getStorePhoto().isEmpty()) {
            if (recommendDTO.getStorePhotoUrl() == null || recommendDTO.getStorePhotoUrl().isEmpty()) {
                bindingResult.rejectValue("storePhoto", "error.recommendDTO", "請上傳店家圖片！");
            }
        }

        if (recommendDTO.getReason() == null || recommendDTO.getReason().trim().isEmpty()) {
            bindingResult.rejectValue("reason", "error.recommendDTO", "推薦原因不能為空！");
        }

        if (recommendDTO.getScore() == null || recommendDTO.getScore() < 1 || recommendDTO.getScore() > 5) {
            bindingResult.rejectValue("score", "error.recommendDTO", "請選擇有效的星級評分！");
        }

        if (bindingResult.hasErrors()) {
            logger.warn("新增推薦表單驗證失敗。");
            model.addAttribute("pageTitle", "新增我的推薦");
            model.addAttribute("recommendDTO", recommendDTO);
            return "recommend-form";
        }

        try {
            if (recommendDTO.getStorePhoto() != null && !recommendDTO.getStorePhoto().isEmpty()) {
                String photoUrl = fileStorageService.uploadFile(recommendDTO.getStorePhoto());
                recommendDTO.setStorePhotoUrl(photoUrl);
            }
            recommendService.addRecommend(recommendDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "推薦新增成功！");
            logger.info("用戶 {} 新增推薦成功，導向個人推薦列表頁。", currentUser.getEmail());
            return "redirect:/personal-recommend";
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            logger.error("新增推薦失敗：{}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/personal-recommend/add";
        } catch (Exception e) {
            logger.error("新增推薦失敗：{}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "新增推薦失敗，請重試。");
            return "redirect:/personal-recommend/add";
        }
    }

    // 處理刪除推薦
    @PostMapping("/personal-recommend/delete/{storeId}")
    public String deleteRecommend(@PathVariable("storeId") Integer storeId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        UserModel currentUser = (UserModel) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }
        try {
            recommendService.deleteRecommend(currentUser.getId(), storeId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "推薦已成功刪除。");
        } catch (EntityNotFoundException | SecurityException e) {
            logger.error("刪除推薦失敗：{}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("刪除推薦失敗：{}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "刪除推薦失敗，請重試。");
        }
        return "redirect:/personal-recommend";
    }

    // 顯示編輯推薦的表單
    @GetMapping("/personal-recommend/edit/{storeId}")
    public String showEditRecommendForm(@PathVariable("storeId") Integer storeId,
                                        Model model,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        UserModel currentUser = (UserModel) session.getAttribute("user");
        if (currentUser == null) {
            logger.warn("未登入用戶嘗試訪問編輯推薦頁面。");
            return "redirect:/login";
        }
        try {
            Optional<RecommendDTO> recommendDTOOptional = recommendService.getRecommendByUserAndStoreId(currentUser, storeId);
            if (recommendDTOOptional.isPresent()) {
                model.addAttribute("pageTitle", "編輯我的推薦");
                model.addAttribute("recommendDTO", recommendDTOOptional.get());
                return "recommend-form";
            } else {
                logger.warn("用戶 {} 嘗試編輯不存在或無權限的推薦，店家ID: {}", currentUser.getId(), storeId);
                redirectAttributes.addFlashAttribute("errorMessage", "找不到該推薦或您無權編輯。");
                return "redirect:/personal-recommend";
            }
        } catch (Exception e) {
            logger.error("加載編輯表單失敗，用戶ID: {}, 店家ID: {}. 錯誤訊息: {}", currentUser.getId(), storeId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "加載編輯表單失敗：" + e.getMessage());
            return "redirect:/personal-recommend";
        }
    }

    // 處理編輯推薦的提交
    @PostMapping("/personal-recommend/edit/{storeId}")
    public String updateRecommendProcess(@PathVariable("storeId") Integer pathStoreId,
                                         @ModelAttribute("recommendDTO") @Validated RecommendDTO recommendDTO,
                                         BindingResult bindingResult,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes,
                                         Model model) {
        UserModel currentUser = (UserModel) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!pathStoreId.equals(recommendDTO.getStoreId())) {
            bindingResult.rejectValue("storeId", "error.recommendDTO", "無效的編輯請求，店家ID不匹配。");
        }

        if (recommendDTO.getStoreName() == null || recommendDTO.getStoreName().trim().isEmpty()) {
            bindingResult.rejectValue("storeName", "error.recommendDTO", "店家名稱不能為空！");
        }
        if (recommendDTO.getStoreAddress() == null || recommendDTO.getStoreAddress().trim().isEmpty()) {
            bindingResult.rejectValue("storeAddress", "error.recommendDTO", "店家地址不能為空！");
        }
        if (recommendDTO.getScore() == null || recommendDTO.getScore() < 1 || recommendDTO.getScore() > 5) {
            bindingResult.rejectValue("score", "error.recommendDTO", "請選擇有效的星級評分！");
        }

        if (bindingResult.hasErrors()) {
            logger.warn("編輯推薦表單驗證失敗。");
            model.addAttribute("pageTitle", "編輯我的推薦");
            model.addAttribute("recommendDTO", recommendDTO);
            return "recommend-form";
        }
        try {
            if (recommendDTO.getStorePhoto() != null && !recommendDTO.getStorePhoto().isEmpty()) {
                String photoUrl = fileStorageService.uploadFile(recommendDTO.getStorePhoto());
                recommendDTO.setStorePhotoUrl(photoUrl);
            } else {
                Optional<StoreModel> existingStore = storeService.getStoreById(recommendDTO.getStoreId());
                existingStore.ifPresent(s -> recommendDTO.setStorePhotoUrl(s.getPhotoUrl()));
            }

            recommendService.updateRecommend(recommendDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "推薦更新成功！");
            return "redirect:/personal-recommend";
        } catch (SecurityException | EntityNotFoundException e) {
            logger.error("更新推薦失敗：{}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "編輯我的推薦");
            model.addAttribute("recommendDTO", recommendDTO);
            return "recommend-form";
        } catch (Exception e) {
            logger.error("更新推薦失敗：{}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "更新推薦失敗，請重試。");
            model.addAttribute("pageTitle", "編輯我的推薦");
            model.addAttribute("recommendDTO", recommendDTO);
            return "recommend-form";
        }
    }
}