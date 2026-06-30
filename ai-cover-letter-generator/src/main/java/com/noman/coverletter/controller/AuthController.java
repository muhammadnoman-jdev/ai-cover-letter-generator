package com.noman.coverletter.controller;

import com.noman.coverletter.dto.RegisterRequestDTO;
import com.noman.coverletter.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequestDTO());
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(
            @Valid @ModelAttribute("registerRequest") RegisterRequestDTO dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) return "register";

        try {
            userService.registerUser(dto);
            return "redirect:/login?verify";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // ── Email Verification ─────────────────────────
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        String result = userService.verifyEmail(token);
        if (result.equals("success")) {
            return "redirect:/login?verified";
        } else if (result.equals("expired")) {
            model.addAttribute("error", "Verification link has expired. Please register again.");
            return "verify-result";
        } else {
            model.addAttribute("error", "Invalid verification link.");
            return "verify-result";
        }
    }

    // ── Forgot Password ────────────────────────────
    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email, Model model) {
        try {
            userService.initiatePasswordReset(email);
            model.addAttribute("success", "Password reset link sent to your email.");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "forgot-password";
    }

    // ── Reset Password ─────────────────────────────
    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam("token") String token, Model model) {
        String result = userService.validatePasswordResetToken(token);
        if (result.equals("invalid")) {
            model.addAttribute("error", "Invalid reset link.");
            return "verify-result";
        } else if (result.equals("expired")) {
            model.addAttribute("error", "Reset link has expired. Please request a new one.");
            return "verify-result";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        try {
            userService.resetPassword(token, password);
            return "redirect:/login?passwordReset";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "reset-password";
        }
    }
}