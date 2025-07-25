package com.example.topfoodnow.controller;

import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.dto.LoginRequestDTO;
import com.example.topfoodnow.dto.ForgotPasswordRequestDTO;
import com.example.topfoodnow.dto.ResetPasswordRequestDTO;
import com.example.topfoodnow.service.UserService;
import com.example.topfoodnow.service.MailService;
import com.example.topfoodnow.service.LogService;
import com.example.topfoodnow.dto.AuthResponseDTO;
import com.example.topfoodnow.dto.UserProfileUpdateDTO;
import com.example.topfoodnow.util.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.Principal;
import java.util.Optional;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "用戶認證與管理")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${app.base-url}")
    private String appBaseUrl;

    private final UserService userService;
    private final MailService mailService;
    private final LogService logService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    // region 輔助方法
    /**
     * @param principal Spring Security 提供的當前認證用戶資訊
     * @return 如果成功取得到當前用戶的 ID，則返回該 ID (Integer)；否則返回 null，表示用戶未登入或系統中不存在與 Principal 關聯的用戶
     */
    private Integer getCurrentUserId(Principal principal) {
        if (principal == null) {
            logger.debug("getCurrentUserId: Principal is null, user not logged in.");
            return null;
        }
        String principalName = principal.getName();
        Optional<UserModel> userOptional = userService.findByEmail(principalName);
        if (userOptional.isPresent()) {
            logger.debug("getCurrentUserId: Found UserModel for email {}, ID: {}", principalName, userOptional.get().getId());
            return userOptional.get().getId();
        } else {
            logger.warn("getCurrentUserId: UserService 找不到對應於郵箱 {} 的用戶。這可能表示數據不一致", principalName);
            return null;
        }
    }
    // endregion

    // region 註冊
    @Operation(
        summary = "註冊",
        description = "接收用戶提交的註冊信息，驗證後創建新用戶並發送驗證郵件。返回註冊結果",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "用戶註冊數據，JSON 格式",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserModel.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "用戶註冊成功，帳戶待驗證"),
            @ApiResponse(responseCode = "400", description = "請求數據無效或電子郵件已被註冊"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤，如郵件發送失敗")
        }
    )
    @PostMapping("/register")
    public ResponseEntity<String> registerProcess(@Valid @RequestBody UserModel user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            logger.warn("註冊表單驗證失敗，錯誤數量: {}", bindingResult.getErrorCount());
            return ResponseEntity.badRequest().body("註冊數據無效: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        try {
            if (userService.findByEmail(user.getEmail()).isPresent()) {
                logger.warn("註冊嘗試：電子郵件 {} 已被註冊", user.getEmail());
                return ResponseEntity.badRequest().body("該電子郵件已被註冊，請使用其他信箱");
            }
            userService.addUser(user);
            logger.info("新用戶 {} 註冊成功，驗證信已發送", user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body("用戶註冊成功，請檢查您的信箱完成帳戶驗證");
        } catch (Exception e) {
            logger.error("用戶註冊失敗，Email: {}. 錯誤訊息: {}", user.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("註冊失敗，請稍後再試");
        }
    }
    // endregion

    // region 登入
    @Operation(
            summary = "登入",
            description = "接收用戶登入憑證（信箱和密碼），驗證成功後返回 JWT Token，失敗則返回錯誤",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "用戶登入憑證，JSON 格式",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "登入成功，返回 JWT Token"),
                    @ApiResponse(responseCode = "401", description = "信箱或密碼錯誤，或帳戶未啟用")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginProcess(@Valid @RequestBody LoginRequestDTO loginRequest,
                                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            logger.warn("登入請求數據無效");
            return ResponseEntity.badRequest().body(new AuthResponseDTO(false, "登入數據無效", null, null));
        }

        try {
            // 使用 AuthenticationManager 進行身份驗證
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        } catch (DisabledException e) {
            logger.warn("登入嘗試失敗：用戶 {} 帳戶未啟用", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(false, "您的帳戶尚未啟用，請檢查您的信箱完成驗證", null, null));
        } catch (BadCredentialsException e) {
            logger.warn("登入嘗試失敗：Email 或密碼不正確，嘗試登入的 Email: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(false, "信箱或密碼不正確，請再試一次", null, null));
        }

        // 如果身份驗證成功，則生成 JWT token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);

        logger.info("用戶 {} 登入成功，已生成 JWT Token", loginRequest.getEmail());
        return ResponseEntity.ok(new AuthResponseDTO(true, "登入成功", jwt, userDetails.getUsername()));
    }
    // endregion

    // region 重設密碼
    @Operation(
        summary = "重設密碼",
        description = "接收用戶信箱，如果存在則發送密碼重設連結到該信箱。無論信箱是否存在，都返回成功信息以防止信息洩露",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "忘記密碼請求，JSON 格式",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ForgotPasswordRequestDTO.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "重設密碼連結已發送（或已處理請求）"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤，如郵件發送失敗")
        }
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email cannot be empty.");
        }
        try {
            userService.createPasswordResetTokenForUser(email);
            return ResponseEntity.ok("Password reset link sent to your email if the account exists.");
        } catch (RuntimeException e) {
            // 為了安全，即使 email 不存在也不要返回詳細錯誤信息
            // 只是返回成功信息，但實際上可能沒有發送郵件
            return ResponseEntity.ok("Password reset link sent to your email if the account exists.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error sending password reset email: " + e.getMessage());
        }
    }
    // endregion

    // region 驗證密碼重設 Token
    @Operation(
        summary = "驗證密碼重設Token",
        description = "驗證密碼重設Token的有效性。如果有效，返回成功狀態；如果無效，返回錯誤狀態",
        parameters = {
            @Parameter(name = "token", description = "密碼重設Token", required = true, example = "a1b2c3d4e5f6")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Token有效，可以進行密碼重設"),
            @ApiResponse(responseCode = "400", description = "Token無效或已過期")
        }
    )
    @GetMapping("/reset-password/validate-token")
    public ResponseEntity<?> validatePasswordResetToken(@RequestParam("token") String token) {
        String result = userService.validatePasswordResetToken(token);
        if ("valid".equals(result)) {
            return ResponseEntity.ok("Token is valid.");
        } else if ("expired".equals(result)) {
            return ResponseEntity.badRequest().body("Token has expired.");
        } else { // "invalid"
            return ResponseEntity.badRequest().body("Invalid token.");
        }
    }

    // endregion

    // region 執行密碼重設
    @Operation(
        summary = "執行密碼重設",
        description = "接收重設密碼Token及新密碼，驗證後更新用戶密碼",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "新密碼數據，JSON 格式",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResetPasswordRequestDTO.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "密碼重設成功"),
            @ApiResponse(responseCode = "400", description = "新密碼和確認密碼不一致、密碼長度不足或Token無效/過期")
        }
    )
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty() || confirmPassword == null || confirmPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Token, new password, and confirmation password cannot be empty.");
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body("New password and confirmation password do not match.");
        }

        try {
            userService.changeUserPassword(token, newPassword);
            return ResponseEntity.ok("Password has been reset successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // 例如 "Invalid password reset token." 或 "Password reset token has expired."
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error resetting password: " + e.getMessage());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("code") String verificationCode) {
        logger.info("收到帳戶驗證請求，驗證碼: {}", verificationCode);
        boolean verified = userService.verifyAccount(verificationCode);

        if (verified) {
            logger.info("帳戶驗證成功，驗證碼: {}", verificationCode);
            return ResponseEntity.ok("您的帳戶已成功開通！");
        } else {
            logger.warn("帳戶驗證失敗或驗證碼無效/已過期，驗證碼: {}", verificationCode);
            return ResponseEntity.badRequest().body("帳戶開通失敗或驗證碼無效/已過期");
        }
    }
    // endregion

    // region 會員中心
    @Operation(
        summary = "取得當前用戶資料",
        description = "需要認證。取得當前登入用戶的詳細資料",
        responses = {
            @ApiResponse(responseCode = "200", description = "成功取得用戶資料",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserModel.class))),
            @ApiResponse(responseCode = "401", description = "未經認證"),
            @ApiResponse(responseCode = "404", description = "用戶不存在")
        }
    )
    @GetMapping("/profile")
    public ResponseEntity<UserModel> getUserProfile(Principal principal) {
        Integer currentUserId = getCurrentUserId(principal);
        if (currentUserId == null) {
            logger.warn("未認證用戶嘗試取得個人資料");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserModel user = userService.findById(currentUserId)
                .orElseThrow(() -> {
                    logger.error("取得個人資料失敗：用戶 ID {} 不存在", currentUserId);
                    return new RuntimeException("用戶不存在");
                });
        logger.info("用戶 ID {} 成功取得個人資料", currentUserId);
        return ResponseEntity.ok(user);
    }

    @Operation(
        summary = "更新當前用戶資料",
        description = "需要認證。允許用戶更新其用戶名、YouTube 和 Instagram 連結",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "用戶更新資料，JSON 格式",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserProfileUpdateDTO.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "用戶資料更新成功",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserModel.class))),
            @ApiResponse(responseCode = "400", description = "請求數據無效"),
            @ApiResponse(responseCode = "401", description = "未經認證"),
            @ApiResponse(responseCode = "404", description = "用戶不存在")
        }
    )
    @PutMapping("/profile")
    public ResponseEntity<UserModel> updateProfile(@Valid @RequestBody UserProfileUpdateDTO updateDTO,
                                                   BindingResult bindingResult,
                                                   Principal principal) {
        Integer currentUserId = getCurrentUserId(principal);
        if (currentUserId == null) {
            logger.warn("未認證用戶嘗試更新個人資料");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (bindingResult.hasErrors()) {
            logger.warn("更新個人資料表單驗證失敗");
            // 為了提供更詳細的錯誤信息給前端，可以返回 bindingResult.getAllErrors()
            return ResponseEntity.badRequest().body(null);
        }

        try {
            UserModel updatedUser = userService.updateProfile(currentUserId, updateDTO);
            logger.info("用戶 ID {} 成功更新個人資料", currentUserId);
            return ResponseEntity.ok(updatedUser);
        } catch (EntityNotFoundException e) {
            logger.error("更新個人資料失敗：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("更新個人資料時發生未知錯誤：{}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    // endregion
}