package com.example.topfoodnow.controller;

import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.dto.RecommendDTO;
import com.example.topfoodnow.dto.LoginRequestDTO;
import com.example.topfoodnow.dto.ForgotPasswordRequestDTO;
import com.example.topfoodnow.dto.ResetPasswordRequestDTO;
import com.example.topfoodnow.service.UserService;
import com.example.topfoodnow.service.MailService;
import com.example.topfoodnow.service.RecommendService;
import com.example.topfoodnow.service.FileStorageService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Tag(name = "用戶與推薦管理", description = "處理用戶註冊、登入、密碼重設及個人推薦相關的Web頁面和表單提交。")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${app.base-url}")
    private String appBaseUrl;

    private final UserService userService;
    private final MailService mailService;
    private final RecommendService recommendService;
    private final FileStorageService fileStorageService;

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        UserModel user = new UserModel();
        user.setIsFamous(false);
        model.addAttribute("user", user);
        return "register";
    }

    @Operation(
        summary = "處理用戶註冊",
        description = "接收用戶提交的註冊信息，驗證後創建新用戶並發送驗證郵件。成功後重定向到註冊成功頁面，失敗則返回註冊頁並顯示錯誤。",
        requestBody = @RequestBody(
            description = "用戶註冊數據",
            required = true,
            content = @Content(
                mediaType = "application/x-www-form-urlencoded",
                schema = @Schema(implementation = UserModel.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "302", description = "註冊成功，重定向至註冊成功頁面"),
            @ApiResponse(responseCode = "200", description = "表單驗證失敗或郵件已註冊，返回註冊頁面並顯示錯誤",
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤",
                content = @Content(schema = @Schema(implementation = String.class)))
        }
    )
    @PostMapping("/register")
    public String registerProcess(@Valid UserModel user,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            logger.warn("註冊表單驗證失敗，錯誤數量: {}", bindingResult.getErrorCount());
            model.addAttribute("user", user);
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

    @Operation(summary = "顯示註冊成功頁面", description = "返回註冊成功後的HTML確認頁面。")
    @ApiResponse(responseCode = "200", description = "成功返回註冊成功頁面HTML")
    @GetMapping("/register-success")
    public String showRegisterSuccessPage(Model model) {
        model.addAttribute("pageTitle", "註冊成功");
        return "register-success";
    }

    @Operation(
        summary = "顯示登入頁面",
        description = "返回用戶登入的HTML表單頁面。可選地顯示錯誤、登出或郵件發送成功訊息。",
        parameters = {
            @Parameter(name = "error", description = "登入失敗錯誤訊息", example = "信箱或密碼錯誤", required = false),
            @Parameter(name = "logout", description = "登出成功訊息", example = "您已成功登出", required = false),
            @Parameter(name = "emailSent", description = "驗證信發送成功訊息", example = "驗證信已發送", required = false)
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "成功返回登入頁面HTML")
        }
    )
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

    @Operation(
        summary = "處理用戶登入",
        description = "接收用戶提交的登入憑證（信箱和密碼），驗證成功後設置會話並重定向到首頁。失敗則返回登入頁並顯示錯誤。",
        requestBody = @RequestBody(
            description = "用戶登入憑證",
            required = true,
            content = @Content(
                mediaType = "application/x-www-form-urlencoded",
                schema = @Schema(implementation = LoginRequestDTO.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "302", description = "登入成功，重定向至首頁"),
            @ApiResponse(responseCode = "200", description = "登入失敗，返回登入頁面並顯示錯誤",
                content = @Content(schema = @Schema(implementation = String.class)))
        }
    )
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

    @Operation(
        summary = "顯示個人推薦列表頁面",
        description = "用戶登入後，顯示其個人推薦的餐廳列表頁面。",
        responses = {
            @ApiResponse(responseCode = "200", description = "成功返回個人推薦列表HTML"),
            @ApiResponse(responseCode = "302", description = "用戶未登入，重定向至登入頁")
        }
    )
    @GetMapping("/personal-recommend")
    public String showPersonalRecommendPage(Model model, HttpSession session) {
        UserModel currentUser = (UserModel) session.getAttribute("user");
        if (currentUser == null) {
            logger.warn("未登入用戶嘗試訪問個人推薦頁面。");
            return "redirect:/login";
        }
        try {
            List<RecommendDTO> recommends = recommendService.getRecommendsByUserId(currentUser.getId());
            model.addAttribute("recommends", recommends);
            model.addAttribute("pageTitle", "我的推薦");
            // **新增這一行，將用戶名稱添加到模型中**
            model.addAttribute("userName", currentUser.getUserName());
            return "personal-recommend";
        } catch (Exception e) {
            logger.error("加載個人推薦列表失敗，用戶ID: {}. 錯誤訊息: {}", currentUser.getId(), e.getMessage(), e);
            model.addAttribute("errorMessage", "加載推薦列表失敗，請稍後再試。");
            return "error-page";
        }
    }

    @Operation(summary = "用戶登出", description = "清除用戶會話，並重定向到登入頁面。")
    @ApiResponse(responseCode = "302", description = "成功登出，重定向至登入頁")
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

    @Operation(summary = "顯示忘記密碼頁面", description = "返回忘記密碼的HTML表單頁面。")
    @ApiResponse(responseCode = "200", description = "成功返回忘記密碼頁面HTML")
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        model.addAttribute("pageTitle", "忘記密碼");
        return "forgot-password";
    }

    @Operation(
        summary = "處理忘記密碼請求",
        description = "接收用戶信箱，如果存在則發送密碼重設連結到該信箱。",
        requestBody = @RequestBody(
            description = "忘記密碼請求",
            required = true,
            content = @Content(
                mediaType = "application/x-www-form-urlencoded",
                schema = @Schema(implementation = ForgotPasswordRequestDTO.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "已發送重設連結（如果信箱存在），返回忘記密碼頁面顯示訊息",
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "發送郵件失敗或其他伺服器錯誤",
                content = @Content(schema = @Schema(implementation = String.class)))
        }
    )
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

    @Operation(
        summary = "顯示重設密碼頁面",
        description = "驗證重設密碼Token的有效性，並返回重設密碼的HTML表單頁面。無效Token將重定向或顯示錯誤。",
        parameters = {
            @Parameter(name = "token", description = "密碼重設Token", required = true, example = "a1b2c3d4e5f6")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Token有效，成功返回重設密碼頁面HTML"),
            @ApiResponse(responseCode = "302", description = "Token無效或過期，重定向至忘記密碼頁面")
        }
    )
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

    @Operation(
        summary = "處理重設密碼請求",
        description = "接收重設密碼Token及新密碼，驗證後更新用戶密碼。成功後重定向到登入頁，失敗則返回重設密碼頁並顯示錯誤。",
        requestBody = @RequestBody(
            description = "新密碼數據",
            required = true,
            content = @Content(
                mediaType = "application/x-www-form-urlencoded",
                schema = @Schema(implementation = ResetPasswordRequestDTO.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "302", description = "密碼重設成功，重定向至登入頁"),
            @ApiResponse(responseCode = "200", description = "密碼驗證失敗，返回重設密碼頁面顯示錯誤",
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "302", description = "Token無效或過期，重定向至忘記密碼頁面")
        }
    )
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

    @Operation(
        summary = "顯示新增推薦表單頁面",
        description = "返回用於用戶新增餐廳推薦的HTML表單頁面。",
        responses = {
            @ApiResponse(responseCode = "200", description = "成功返回新增推薦表單HTML"),
            @ApiResponse(responseCode = "302", description = "用戶未登入，重定向至登入頁")
        }
    )
    @GetMapping("/personal-recommend/add")
    public String showAddRecommendForm(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        model.addAttribute("recommendDTO", new RecommendDTO());
        return "recommend-form";
    }

    @Operation(
        summary = "處理新增推薦提交",
        description = "接收用戶提交的餐廳推薦信息（包含店家詳情和推薦原因/評分，可選上傳圖片），驗證後保存。成功後重定向到個人推薦列表頁面。",
        requestBody = @RequestBody(
            description = "推薦信息和可選的店家圖片",
            required = true,
            content = @Content(
                mediaType = "multipart/form-data",
                schema = @Schema(implementation = RecommendDTO.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "302", description = "推薦新增成功，重定向至個人推薦列表頁"),
            @ApiResponse(responseCode = "200", description = "表單驗證失敗，返回新增推薦表單頁面並顯示錯誤",
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "302", description = "用戶未登入或新增失敗，重定向到登入頁或新增頁面並顯示錯誤")
        }
    )
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

    @Operation(
        summary = "顯示編輯推薦表單頁面",
        description = "根據店家ID顯示用戶編輯現有推薦的HTML表單頁面。",
        parameters = {
            @Parameter(name = "storeId", description = "要編輯的店家ID", required = true, example = "101")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "成功返回編輯推薦表單HTML"),
            @ApiResponse(responseCode = "302", description = "用戶未登入、找不到推薦或無權編輯，重定向到登入頁或個人推薦列表頁")
        }
    )
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
            Optional<RecommendDTO> recommendDTOOptional = recommendService.getRecommendByUserAndStoreId(currentUser.getId(), storeId); // <-- 修改這裡
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

    @Operation(
        summary = "處理更新推薦提交",
        description = "接收用戶提交的更新後的餐廳推薦信息（包含店家詳情和推薦原因/評分，可選上傳圖片），驗證後更新保存。成功後重定向到個人推薦列表頁面。",
        parameters = {
            @Parameter(name = "storeId", description = "要更新的店家ID", required = true, example = "101", in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH)
        },
        requestBody = @RequestBody(
            description = "更新後的推薦信息和可選的店家圖片",
            required = true,
            content = @Content(
                mediaType = "multipart/form-data",
                schema = @Schema(implementation = RecommendDTO.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "302", description = "推薦更新成功，重定向至個人推薦列表頁"),
            @ApiResponse(responseCode = "200", description = "表單驗證失敗，返回編輯推薦表單頁面並顯示錯誤",
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "302", description = "用戶未登入、找不到推薦或無權更新，重定向到登入頁或編輯頁面並顯示錯誤")
        }
    )
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

    @Operation(
        summary = "處理刪除推薦請求",
        description = "接收用戶刪除推薦的請求，根據店家ID刪除用戶的推薦。成功後重定向到個人推薦列表頁面。",
        parameters = {
            @Parameter(name = "storeId", description = "要刪除的店家ID", required = true, example = "101", in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH)
        },
        responses = {
            @ApiResponse(responseCode = "302", description = "推薦刪除成功，重定向至個人推薦列表頁"),
            @ApiResponse(responseCode = "302", description = "用戶未登入、找不到推薦或無權刪除，重定向到登入頁或個人推薦列表頁並顯示錯誤")
        }
    )
    @PostMapping("/personal-recommend/delete/{storeId}")
    public String deleteRecommendProcess(@PathVariable("storeId") Integer storeId,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        UserModel currentUser = (UserModel) session.getAttribute("user");
        if (currentUser == null) {
            logger.warn("未登入用戶嘗試刪除推薦，店家ID: {}", storeId);
            return "redirect:/login";
        }

        try {
            recommendService.deleteRecommend(currentUser.getId(), storeId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "推薦已成功刪除！");
            logger.info("用戶 ID: {} 成功刪除對店家 ID: {} 的推薦。", currentUser.getId(), storeId);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            logger.error("刪除推薦失敗：找不到推薦或無權限。用戶ID: {}, 店家ID: {}. 錯誤訊息: {}", currentUser.getId(), storeId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("刪除推薦時發生未知錯誤。用戶ID: {}, 店家ID: {}. 錯誤訊息: {}", currentUser.getId(), storeId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "刪除推薦失敗，請稍後再試。");
        }
        return "redirect:/personal-recommend";
    }
}