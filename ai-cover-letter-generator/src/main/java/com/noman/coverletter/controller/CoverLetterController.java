package com.noman.coverletter.controller;

import com.noman.coverletter.dto.CoverLetterRequestDTO;
import com.noman.coverletter.entity.CoverLetter;
import com.noman.coverletter.service.CoverLetterService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cover-letters")
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    public CoverLetterController(CoverLetterService coverLetterService) {
        this.coverLetterService = coverLetterService;
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("coverLetterRequest", new CoverLetterRequestDTO());
        return "cover-letter-form";
    }

    @PostMapping("/generate")
    public String generate(
            @Valid @ModelAttribute("coverLetterRequest") CoverLetterRequestDTO dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "cover-letter-form";
        }

        CoverLetter coverLetter = coverLetterService.generate(dto);
        return "redirect:/cover-letters/" + coverLetter.getId();
    }

    @GetMapping("/{id}")
    public String viewOne(@PathVariable Long id, Model model) {
        CoverLetter coverLetter = coverLetterService.getByIdForCurrentUser(id);
        model.addAttribute("coverLetter", coverLetter);
        return "cover-letter-view";
    }

    @GetMapping("/history")
    public String history(Model model) {
        List<CoverLetter> letters = coverLetterService.getAllForCurrentUser();
        model.addAttribute("letters", letters);
        return "cover-letter-history";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        coverLetterService.deleteByIdForCurrentUser(id);
        return "redirect:/cover-letters/history";
    }
}