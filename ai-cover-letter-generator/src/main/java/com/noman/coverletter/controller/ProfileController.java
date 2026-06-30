package com.noman.coverletter.controller;

import com.noman.coverletter.dto.PasswordChangeDTO;
import com.noman.coverletter.dto.ProfileUpdateDTO;
import com.noman.coverletter.entity.User;
import com.noman.coverletter.service.CoverLetterService;
import com.noman.coverletter.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProfileController {

    private final UserService userService;
    private final CoverLetterService coverLetterService;

    public ProfileController(UserService userService, CoverLetterService coverLetterService) {
        this.userService = userService;
        this.coverLetterService = coverLetterService;
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        User user = userService.getCurrentUser();

        ProfileUpdateDTO profileDto = new ProfileUpdateDTO();
        profileDto.setName(user.getName());

        model.addAttribute("user", user);
        model.addAttribute("profileUpdate", profileDto);
        model.addAttribute("passwordChange", new PasswordChangeDTO());
        model.addAttribute("totalLetters", coverLetterService.getCountForCurrentUser());

        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("profileUpdate") ProfileUpdateDTO dto, Model model) {
        userService.updateProfile(dto);
        model.addAttribute("profileSuccess", "Profile updated successfully!");
        return "redirect:/profile?updated";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@ModelAttribute("passwordChange") PasswordChangeDTO dto, Model model) {
        try {
            userService.changePassword(dto);
            return "redirect:/profile?passwordChanged";
        } catch (RuntimeException e) {
            User user = userService.getCurrentUser();
            model.addAttribute("user", user);
            model.addAttribute("profileUpdate", new ProfileUpdateDTO());
            model.addAttribute("passwordChange", new PasswordChangeDTO());
            model.addAttribute("totalLetters", coverLetterService.getCountForCurrentUser());
            model.addAttribute("passwordError", e.getMessage());
            return "profile";
        }
    }
}