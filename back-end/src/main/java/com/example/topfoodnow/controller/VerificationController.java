package com.example.topfoodnow.controller;

import com.example.topfoodnow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VerificationController {
    private final UserService userService;

    @Autowired
    public VerificationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/verify")
    public String verifyAccount(@RequestParam("code") String verificationCode, Model model) {
        boolean verified = userService.verifyAccount(verificationCode);

        if (verified) {
            model.addAttribute("message", "您的帳戶已成功開通！");
            return "account-verified";
        } else {
            model.addAttribute("message", "帳戶開通失敗或驗證碼無效/已過期。");
            return "account-verified";
        }
    }
}