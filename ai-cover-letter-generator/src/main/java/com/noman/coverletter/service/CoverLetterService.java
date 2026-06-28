package com.noman.coverletter.service;

import com.noman.coverletter.dto.CoverLetterRequestDTO;
import com.noman.coverletter.entity.CoverLetter;
import com.noman.coverletter.entity.User;
import com.noman.coverletter.repository.CoverLetterRepository;
import com.noman.coverletter.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoverLetterService {

    private final CoverLetterRepository coverLetterRepository;
    private final UserRepository userRepository;
    private final AIService aiService;

    public CoverLetterService(CoverLetterRepository coverLetterRepository,
                               UserRepository userRepository,
                               AIService aiService) {
        this.coverLetterRepository = coverLetterRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public CoverLetter generate(CoverLetterRequestDTO dto) {
        User user = getCurrentUser();

        String generatedContent = aiService.generateCoverLetter(dto);

        CoverLetter coverLetter = new CoverLetter();
        coverLetter.setTitle(dto.getTitle());
        coverLetter.setJobTitle(dto.getJobTitle());
        coverLetter.setCompanyName(dto.getCompanyName());
        coverLetter.setSkills(dto.getSkills());
        coverLetter.setExperience(dto.getExperience());
        coverLetter.setJobDescription(dto.getJobDescription());
        coverLetter.setTone(dto.getTone());
        coverLetter.setGeneratedContent(generatedContent);
        coverLetter.setUser(user);

        return coverLetterRepository.save(coverLetter);
    }

    public List<CoverLetter> getAllForCurrentUser() {
        User user = getCurrentUser();
        return coverLetterRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public CoverLetter getByIdForCurrentUser(Long id) {
        User user = getCurrentUser();
        return coverLetterRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Cover letter not found"));
    }

    public void deleteByIdForCurrentUser(Long id) {
        CoverLetter coverLetter = getByIdForCurrentUser(id);
        coverLetterRepository.delete(coverLetter);
    }
}