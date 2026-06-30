package com.noman.coverletter.controller;

import com.noman.coverletter.service.AIService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/tools")
public class ToolsController {

    private final AIService aiService;

    public ToolsController(AIService aiService) {
        this.aiService = aiService;
    }

    // ── Resume Summary ─────────────────────────────
    @GetMapping("/resume-summary")
    public String showResumeSummary() {
        return "resume-summary";
    }

    @PostMapping("/resume-summary")
    public String generateResumeSummary(
            @RequestParam("name") String name,
            @RequestParam("targetRole") String targetRole,
            @RequestParam("skills") String skills,
            @RequestParam("experience") String experience,
            Model model) {

        String summary = aiService.generateResumeSummary(name, skills, experience, targetRole);
        model.addAttribute("summary", summary);
        model.addAttribute("name", name);
        model.addAttribute("targetRole", targetRole);
        model.addAttribute("skills", skills);
        model.addAttribute("experience", experience);
        return "resume-summary";
    }

    // ── Interview Questions ────────────────────────
    @GetMapping("/interview-questions")
    public String showInterviewQuestions() {
        return "interview-questions";
    }

    @PostMapping("/interview-questions")
    public String generateInterviewQuestions(
            @RequestParam("jobTitle") String jobTitle,
            @RequestParam("skills") String skills,
            Model model) {

        String questions = aiService.generateInterviewQuestions(jobTitle, skills);
        model.addAttribute("questions", questions);
        model.addAttribute("jobTitle", jobTitle);
        model.addAttribute("skills", skills);
        return "interview-questions";
    }
}