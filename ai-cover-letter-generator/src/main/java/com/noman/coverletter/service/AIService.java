package com.noman.coverletter.service;

import com.noman.coverletter.dto.CoverLetterRequestDTO;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    public String generateCoverLetter(CoverLetterRequestDTO dto) {
        // Stub response - I will replace this with Gemini API later
        return "Dear Hiring Manager,\n\n" +
               "I am writing to apply for the position of " + dto.getJobTitle() +
               " at " + dto.getCompanyName() + ".\n\n" +
               "With my experience in " + dto.getSkills() + ", I am confident " +
               "that I would be a great fit for this role.\n\n" +
               dto.getExperience() + "\n\n" +
               "Thank you for considering my application.\n\n" +
               "Sincerely,\nYour Name";
    }
}