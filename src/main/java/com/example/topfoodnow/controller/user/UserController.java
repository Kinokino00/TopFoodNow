package com.example.topfoodnow.controller.user;

import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.controller.recommend.dto.Recommend;
import com.example.topfoodnow.controller.user.dto.LoginRequest;
import com.example.topfoodnow.controller.user.dto.ForgotPasswordRequest;
import com.example.topfoodnow.controller.user.dto.ResetPasswordRequest;
import com.example.topfoodnow.service.user.UserService;
import com.example.topfoodnow.service.mail.MailService;
import com.example.topfoodnow.service.recommend.RecommendService;
import com.example.topfoodnow.service.file.FileStorageService;
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
@Tag(name = "?�戶?�推?�管??, description = "?��??�戶註�??�登?�、�?碼�?設�??�人?�薦?��??�Web?�面?�表?��?交�?)
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
        summary = "?��??�戶註�?",
        description = "?�收?�戶?�交?�註?�信?��?驗�?後創建新?�戶並發?��?證郵件。�??��??��??�到註�??��??�面，失?��?返�?註�??�並顯示?�誤??,
        requestBody = @RequestBody(
            description = "?�戶註�??��?",
            required = true,
            content = @Content(
                mediaType = "application/x-www-form-urlencoded",
                schema = @Schema(implementation = UserModel.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "302", description = "註�??��?，�?定�??�註?��??��???),
            @ApiResponse(responseCode = "200", description = "表單驗�?失�??�郵件已註�?，�??�註?��??�並顯示?�誤",
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "伺�??�內?�錯�?,
                content = @Content(schema = @Schema(implementation = String.class)))
        }
    )
    @PostMapping("/register")
    public String registerProcess(@Valid UserModel user,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            logger.warn("註�?表單驗�?失�?，錯誤數?? {}", bindingResult.getErrorCount());
            model.addAttribute("user", user);
            return "register";
        }

        try {
            if (userService.findByEmail(user.getEmail()).isPresent()) {
                logger.warn("註�??�試：電子郵�?{} 已被註�???, user.getEmail());
                model.addAttribute("error", "該電子郵件已被註?��?請使?�其他信箱�?);
                model.addAttribute("user", user);
                return "register";
            }
            userService.addUser(user);
            logger.info("?�用??{} 註�??��?，�?證信已發?��?, user.getEmail());
            redirectAttributes.addFlashAttribute("email", user.getEmail());
            return "redirect:/register-success";
        } catch (Exception e) {
            logger.error("?�戶註�?失�?，Email: {}. ?�誤訊息: {}", user.getEmail(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "註�?失�?，�?稍�??�試??);
            return "redirect:/register";
        }
    }

    @Operation(summary = "顯示註�??��??�面", description = "返�?註�??��?後�?HTML確�??�面??)
    @ApiResponse(responseCode = "200", description = "?��?返�?註�??��??�面HTML")
    @GetMapping("/register-success")
    public String showRegisterSuccessPage(Model model) {
        model.addAttribute("pageTitle", "註�??��?");
        return "register-success";
    }

    @Operation(
        summary = "顯示?�入?�面",
        description = "返�??�戶?�入?�HTML表單?�面?�可?�地顯示?�誤?�登?��??�件?�送�??��??��?,
        parameters = {
            @Parameter(name = "error", description = "?�入失�??�誤訊息", example = "信箱?��?碼錯�?, required = false),
            @Parameter(name = "logout", description = "?�出?��?訊息", example = "?�已?��??�出", required = false),
            @Parameter(name = "emailSent", description = "驗�?信發?��??��???, example = "驗�?信已?��?, required = false)
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "?��?返�??�入?�面HTML")
        }
    )
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "emailSent", required = false) String emailSent,
                                Model model) {
        model.addAttribute("pageTitle", "?�戶?�入");
        if (error != null) {
            logger.warn("?�入?�面顯示?�誤訊息：{}", error);
            model.addAttribute("error", "信箱?��?碼錯誤�?請�?試�?次�?);
        }
        if (logout != null) {
            logger.info("?�入?�面顯示?�出訊息：{}", logout);
            model.addAttribute("logout", "?�已?��??�出??);
        }
        if (emailSent != null) {
            logger.info("?�入?�面顯示驗�?信已?�送�??��?{}", emailSent);
            model.addAttribute("emailSent", "驗�?信已?�送�?請檢?�您?�信箱以?�用帳戶??);
        }
        return "login";
    }

    @Operation(
        summary = "?��??�戶?�入",
        description = "?�收?�戶?�交?�登?��?證�?信箱?��?碼�?，�?證�??��?設置?�話並�?定�??��??�。失?��?返�??�入?�並顯示?�誤??,
        requestBody = @RequestBody(
            description = "?�戶?�入?��?",
            required = true,
            content = @Content(
                mediaType = "application/x-www-form-urlencoded",
                schema = @Schema(implementation = LoginRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "302", description = "?�入?��?，�?定�??��???),
            @ApiResponse(responseCode = "200", description = "?�入失�?，�??�登?��??�並顯示?�誤",
                content = @Content(schema = @Schema(implementation = String.class)))
        }
    )
    @PostMapping("/login")
    public String loginProcess(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        model.addAttribute("pageTitle", "?�戶?�入");

        UserModel authenticatedUser = userService.authenticate(email, password);
        if (authenticatedUser != null) {
            if (!authenticatedUser.isEnabled()) {
                logger.warn("?�入?�試失�?：用??{} 帳戶?��??��?, email);
                model.addAttribute("error", "?��?帳戶尚未?�用，�?檢查?��?信箱完�?驗�???);
                return "login";
            }
            session.setAttribute("user", authenticatedUser);
            logger.info("?�戶 {} ?�入?��???, email);
            return "redirect:/";
        } else {
            logger.warn("?�入?�試失�?：Email ?��?碼�?�?��，�?試登?��? Email: {}", email);
            model.addAttribute("error", "信箱?��?碼�?�?��，�??�試一次�?);
            return "login";
        }
    }

    @Operation(
        summary = "顯示?�人?�薦?�表?�面",
        description = "?�戶?�入後�?顯示?�個人?�薦?��?廳�?表�??��?,
        responses = {
            @ApiResponse(responseCode = "200", description = "?��?返�??�人?�薦?�表HTML"),
            @ApiResponse(responseCode = "302", description = "?�戶?�登?��??��??�至?�入??)
        }
    )
    @GetMapping("/personal-recommend")
    public String showPersonalRecommendPage(Model model, HttpSession session) {
        UserModel currentUser = (UserModel) session.getAttribute("user");
        if (currentUser == null) {
            logger.warn("?�登?�用?��?試訪?�個人?�薦?�面??);
            return "redirect:/login";
        }
        try {
            List<Recommend> recommends = recommendService.getRecommendsByUserId(currentUser.getId());
            model.addAttribute("recommends", recommends);
            model.addAttribute("pageTitle", "?��??�薦");
            // **?��??��?行�?將用?��?稱添?�到模�?�?*
            model.addAttribute("userName", currentUser.getUserName());
            return "personal-recommend";
        } catch (Exception e) {
            logger.error("?��??�人?�薦?�表失�?，用?�ID: {}. ?�誤訊息: {}", currentUser.getId(), e.getMessage(), e);
            model.addAttribute("errorMessage", "?��??�薦?�表失�?，�?稍�??�試??);
            return "error-page";
        }
    }

    @Operation(summary = "?�戶?�出", description = "清除?�戶?�話，並?��??�到?�入?�面??)
    @ApiResponse(responseCode = "302", description = "?��??�出，�?定�??�登?��?")
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        String userEmail = null;
        if (session.getAttribute("user") instanceof UserModel) {
            userEmail = ((UserModel) session.getAttribute("user")).getEmail();
        }
        session.invalidate();
        logger.info("?�戶 {} ?�出?��???, userEmail != null ? userEmail : "?�知?�戶");
        return "redirect:/login?logout";
    }

    @Operation(summary = "顯示忘�?密碼?�面", description = "返�?忘�?密碼?�HTML表單?�面??)
    @ApiResponse(responseCode = "200", description = "?��?返�?忘�?密碼?�面HTML")
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        model.addAttribute("pageTitle", "忘�?密碼");
        return "forgot-password";
    }

    @Operation(
        summary = "?��?忘�?密碼請�?",
        description = "?�收?�戶信箱，�??��??��??�送�?碼�?設�???�該信箱??,
        requestBody = @RequestBody(
            description = "忘�?密碼請�?",
            required = true,
            content = @Content(
                mediaType = "application/x-www-form-urlencoded",
                schema = @Schema(implementation = ForgotPasswordRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "已發?��?設�??（�??�信箱�??��?，�??��?記�?碼�??�顯示�???,
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "?�送郵件失?��??��?伺�??�錯�?,
                content = @Content(schema = @Schema(implementation = String.class)))
        }
    )
    @PostMapping("/forgot-password")
    public String forgotPasswordProcess(@RequestParam("email") String email,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        model.addAttribute("pageTitle", "忘�?密碼");

        Optional<UserModel> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.info("忘�?密碼請�?：Email {} 不�??�於系統中�?, email);
            model.addAttribute("successMessage", "如�?此信箱已註�?，�?設�?碼�??將�??�送到?��?信箱?��?檢查?��??�件箱�?);
            return "forgot-password";
        }

        UserModel user = userOptional.get();
        try {
            String token = userService.createPasswordResetTokenForUser(user);
            String resetLink = appBaseUrl +  "/reset-password?token=" + token;
            mailService.sendPasswordResetEmail(user.getEmail(), user.getUserName(), resetLink);
            logger.info("?�戶 {} ?��?碼�?設�??已�??�並?�送�?, user.getEmail());
            model.addAttribute("successMessage", "?�設密碼???已發?��?請檢?�您?�信箱�?);
        } catch (Exception e) {
            logger.error("?��?忘�?密碼請�?失�?，Email: {}. ?�誤訊息: {}", email, e.getMessage(), e);
            model.addAttribute("errorMessage", "?�送�?設�???�發?�錯誤�?請�?後�?試�?);
        }
        return "forgot-password";
    }

    @Operation(
        summary = "顯示?�設密碼?�面",
        description = "驗�??�設密碼Token?��??�性�?並�??��?設�?碼�?HTML表單?�面?�無?�Token將�?定�??�顯示錯誤�?,
        parameters = {
            @Parameter(name = "token", description = "密碼?�設Token", required = true, example = "a1b2c3d4e5f6")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Token?��?，�??��??��?設�?碼�??�HTML"),
            @ApiResponse(responseCode = "302", description = "Token?��??��??��??��??�至忘�?密碼?�面")
        }
    )
    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token, Model model) {
        model.addAttribute("pageTitle", "?�設密碼");

        Optional<UserModel> userOptional = userService.validatePasswordResetToken(token);
        if (userOptional.isEmpty()) {
            logger.warn("顯示?�設密碼?�面失�?：無?��?已�???Token: {}", token);
            model.addAttribute("errorMessage", "?��??�已?��??��?設�?碼�???��??�新?��???);
            return "forgot-password";
        }
        logger.info("顯示?�設密碼?�面?��?，Token: {}", token);
        model.addAttribute("token", token);
        return "reset-password";
    }

    @Operation(
        summary = "?��??�設密碼請�?",
        description = "?�收?�設密碼Token?�新密碼，�?證�??�新?�戶密碼?��??��??��??�到?�入?��?失�??��??��?設�?碼�?並顯示錯誤�?,
        requestBody = @RequestBody(
            description = "?��?碼數??,
            required = true,
            content = @Content(
                mediaType = "application/x-www-form-urlencoded",
                schema = @Schema(implementation = ResetPasswordRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "302", description = "密碼?�設?��?，�?定�??�登?��?"),
            @ApiResponse(responseCode = "200", description = "密碼驗�?失�?，�??��?設�?碼�??�顯示錯�?,
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "302", description = "Token?��??��??��??��??�至忘�?密碼?�面")
        }
    )
    @PostMapping("/reset-password")
    public String resetPasswordProcess(@RequestParam("token") String token,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        model.addAttribute("pageTitle", "?�設密碼");

        if (!newPassword.equals(confirmPassword)) {
            logger.warn("?�設密碼失�?：Token {} ?�新密碼?�確認�?碼�?一?��?, token);
            model.addAttribute("errorMessage", "?��?碼�?確�?密碼不�??��?);
            model.addAttribute("token", token);
            return "reset-password";
        }

        if (newPassword.length() < 8) {
            logger.warn("?�設密碼失�?：Token {} ?��?碼長度�?足�?, token);
            model.addAttribute("errorMessage", "密碼?�度?��???8 ?��?符�?);
            model.addAttribute("token", token);
            return "reset-password";
        }

        Optional<UserModel> userOptional = userService.validatePasswordResetToken(token);
        if (userOptional.isEmpty()) {
            logger.warn("?�設密碼?��?失�?：無?��?已�???Token: {}", token);
            redirectAttributes.addFlashAttribute("errorMessage", "?��??�已?��??��?設�?碼�???��??�新?��???);
            return "redirect:/forgot-password";
        }

        UserModel user = userOptional.get();
        userService.changeUserPassword(user, newPassword);
        logger.info("?�戶 {} ?��?碼已?��??�設??, user.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "?��?密碼已�??��?設�?請使?�新密碼?�入??);
        return "redirect:/login";
    }

    @Operation(
        summary = "顯示?��??�薦表單?�面",
        description = "返�??�於?�戶?��?餐廳?�薦?�HTML表單?�面??,
        responses = {
            @ApiResponse(responseCode = "200", description = "?��?返�??��??�薦表單HTML"),
            @ApiResponse(responseCode = "302", description = "?�戶?�登?��??��??�至?�入??)
        }
    )
    @GetMapping("/personal-recommend/add")
    public String showAddRecommendForm(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        model.addAttribute("Recommend", new Recommend());
        return "recommend-form";
    }

    @Operation(
        summary = "?��??��??�薦?�交",
        description = "?�收?�戶?�交?��?廳推?�信?��??�含店家詳�??�推?��???評�?，可?��??��??��?，�?證�?保�??��??��??��??�到?�人?�薦?�表?�面??,
        requestBody = @RequestBody(
            description = "?�薦信息?�可?��?店家?��?",
            required = true,
            content = @Content(
                mediaType = "multipart/form-data",
                schema = @Schema(implementation = Recommend.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "302", description = "?�薦?��??��?，�?定�??�個人?�薦?�表??),
            @ApiResponse(responseCode = "200", description = "表單驗�?失�?，�??�新增推?�表?��??�並顯示?�誤",
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "302", description = "?�戶?�登?��??��?失�?，�?定�??�登?��??�新增�??�並顯示?�誤")
        }
    )
    @PostMapping("/personal-recommend/add")
    public String addRecommendProcess(@ModelAttribute("Recommend") @Validated Recommend Recommend,
                                      BindingResult bindingResult,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        UserModel currentUser = (UserModel) session.getAttribute("user");
        if (currentUser == null) {
            logger.warn("?�登?�用?��?試新增推?��?);
            return "redirect:/login";
        }
        logger.info("?��??�入?�戶 ID: {}", currentUser.getId());
        logger.info("?��??�入?�戶 Email: {}", currentUser.getEmail());

        if (Recommend.getStorePhoto() == null || Recommend.getStorePhoto().isEmpty()) {
            if (Recommend.getStorePhotoUrl() == null || Recommend.getStorePhotoUrl().isEmpty()) {
                bindingResult.rejectValue("storePhoto", "error.Recommend", "請�??��?家�??��?");
            }
        }

        if (Recommend.getReason() == null || Recommend.getReason().trim().isEmpty()) {
            bindingResult.rejectValue("reason", "error.Recommend", "?�薦?��?不能?�空�?);
        }

        if (Recommend.getScore() == null || Recommend.getScore() < 1 || Recommend.getScore() > 5) {
            bindingResult.rejectValue("score", "error.Recommend", "請選?��??��??��?評�?�?);
        }

        if (bindingResult.hasErrors()) {
            logger.warn("?��??�薦表單驗�?失�???);
            model.addAttribute("Recommend", Recommend);
            return "recommend-form";
        }

        try {
            if (Recommend.getStorePhoto() != null && !Recommend.getStorePhoto().isEmpty()) {
                String photoUrl = fileStorageService.uploadFile(Recommend.getStorePhoto());
                Recommend.setStorePhotoUrl(photoUrl);
            }
            recommendService.addRecommend(Recommend, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "?�薦?��??��?�?);
            logger.info("?�戶 {} ?��??�薦?��?，�??�個人?�薦?�表?��?, currentUser.getEmail());
            return "redirect:/personal-recommend";
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            logger.error("?��??�薦失�?：{}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/personal-recommend/add";
        } catch (Exception e) {
            logger.error("?��??�薦失�?：{}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "?��??�薦失�?，�??�試??);
            return "redirect:/personal-recommend/add";
        }
    }

    @Operation(
        summary = "顯示編輯?�薦表單?�面",
        description = "?��?店家ID顯示?�戶編輯?��??�薦?�HTML表單?�面??,
        parameters = {
            @Parameter(name = "storeId", description = "要編輯�?店家ID", required = true, example = "101")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "?��?返�?編輯?�薦表單HTML"),
            @ApiResponse(responseCode = "302", description = "?�戶?�登?�、找不到?�薦?�無權編輯�??��??�到?�入?��??�人?�薦?�表??)
        }
    )
    @GetMapping("/personal-recommend/edit/{storeId}")
    public String showEditRecommendForm(@PathVariable("storeId") Integer storeId,
                                        Model model,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        UserModel currentUser = (UserModel) session.getAttribute("user");
        if (currentUser == null) {
            logger.warn("?�登?�用?��?試訪?�編輯推?��??��?);
            return "redirect:/login";
        }
        try {
            Optional<Recommend> RecommendOptional = recommendService.getRecommendByUserAndStoreId(currentUser.getId(), storeId); // <-- 修改?�裡
            if (RecommendOptional.isPresent()) {
                model.addAttribute("pageTitle", "編輯?��??�薦");
                model.addAttribute("Recommend", RecommendOptional.get());
                return "recommend-form";
            } else {
                logger.warn("?�戶 {} ?�試編輯不�??��??��??��??�薦，�?家ID: {}", currentUser.getId(), storeId);
                redirectAttributes.addFlashAttribute("errorMessage", "?��??�該?�薦?�您?��?編輯??);
                return "redirect:/personal-recommend";
            }
        } catch (Exception e) {
            logger.error("?��?編輯表單失�?，用?�ID: {}, 店家ID: {}. ?�誤訊息: {}", currentUser.getId(), storeId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "?��?編輯表單失�?�? + e.getMessage());
            return "redirect:/personal-recommend";
        }
    }

    @Operation(
        summary = "?��??�新?�薦?�交",
        description = "?�收?�戶?�交?�更?��??��?廳推?�信?��??�含店家詳�??�推?��???評�?，可?��??��??��?，�?證�??�新保�??��??��??��??�到?�人?�薦?�表?�面??,
        parameters = {
            @Parameter(name = "storeId", description = "要更?��?店家ID", required = true, example = "101", in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH)
        },
        requestBody = @RequestBody(
            description = "?�新後�??�薦信息?�可?��?店家?��?",
            required = true,
            content = @Content(
                mediaType = "multipart/form-data",
                schema = @Schema(implementation = Recommend.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "302", description = "?�薦?�新?��?，�?定�??�個人?�薦?�表??),
            @ApiResponse(responseCode = "200", description = "表單驗�?失�?，�??�編輯推?�表?��??�並顯示?�誤",
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "302", description = "?�戶?�登?�、找不到?�薦?�無權更?��??��??�到?�入?��?編輯?�面並顯示錯�?)
        }
    )
    @PostMapping("/personal-recommend/edit/{storeId}")
    public String updateRecommendProcess(@PathVariable("storeId") Integer pathStoreId,
                                         @ModelAttribute("Recommend") @Validated Recommend Recommend,
                                         BindingResult bindingResult,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes,
                                         Model model) {
        UserModel currentUser = (UserModel) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!pathStoreId.equals(Recommend.getStoreId())) {
            bindingResult.rejectValue("storeId", "error.Recommend", "?��??�編輯�?求�?店家ID不匹?��?);
        }

        if (bindingResult.hasErrors()) {
            logger.warn("編輯?�薦表單驗�?失�???);
            model.addAttribute("pageTitle", "編輯?��??�薦");
            model.addAttribute("Recommend", Recommend);
            return "recommend-form";
        }

        try {
            if (Recommend.getStorePhoto() != null && !Recommend.getStorePhoto().isEmpty()) {
                String photoUrl = fileStorageService.uploadFile(Recommend.getStorePhoto());
                Recommend.setStorePhotoUrl(photoUrl);
            }
            recommendService.updateRecommend(Recommend, currentUser);

            redirectAttributes.addFlashAttribute("successMessage", "?�薦?�新?��?�?);
            return "redirect:/personal-recommend";
        } catch (SecurityException | EntityNotFoundException e) {
            logger.error("?�新?�薦失�?：{}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "編輯?��??�薦");
            model.addAttribute("Recommend", Recommend);
            return "recommend-form";
        } catch (Exception e) {
            logger.error("?�新?�薦失�?：{}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "?�新?�薦失�?，�??�試??);
            model.addAttribute("pageTitle", "編輯?��??�薦");
            model.addAttribute("Recommend", Recommend);
            return "recommend-form";
        }
    }

    @Operation(
        summary = "?��??�除?�薦請�?",
        description = "?�收?�戶?�除?�薦?��?求�??��?店家ID?�除?�戶?�推?�。�??��??��??�到?�人?�薦?�表?�面??,
        parameters = {
            @Parameter(name = "storeId", description = "要刪?��?店家ID", required = true, example = "101", in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH)
        },
        responses = {
            @ApiResponse(responseCode = "302", description = "?�薦?�除?��?，�?定�??�個人?�薦?�表??),
            @ApiResponse(responseCode = "302", description = "?�戶?�登?�、找不到?�薦?�無權刪?��??��??�到?�入?��??�人?�薦?�表?�並顯示?�誤")
        }
    )
    @PostMapping("/personal-recommend/delete/{storeId}")
    public String deleteRecommendProcess(@PathVariable("storeId") Integer storeId,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        UserModel currentUser = (UserModel) session.getAttribute("user");
        if (currentUser == null) {
            logger.warn("?�登?�用?��?試刪?�推?��?店家ID: {}", storeId);
            return "redirect:/login";
        }

        try {
            recommendService.deleteRecommend(currentUser.getId(), storeId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "?�薦已�??�刪?��?");
            logger.info("?�戶 ID: {} ?��??�除對�?�?ID: {} ?�推?��?, currentUser.getId(), storeId);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            logger.error("?�除?�薦失�?：找不到?�薦?�無權�??�用?�ID: {}, 店家ID: {}. ?�誤訊息: {}", currentUser.getId(), storeId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("?�除?�薦?�發?�未?�錯誤。用?�ID: {}, 店家ID: {}. ?�誤訊息: {}", currentUser.getId(), storeId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "?�除?�薦失�?，�?稍�??�試??);
        }
        return "redirect:/personal-recommend";
    }
}
