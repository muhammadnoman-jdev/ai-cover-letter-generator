package com.noman.coverletter.service;

import com.noman.coverletter.dto.CoverLetterRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public AIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateCoverLetter(CoverLetterRequestDTO dto) {
        String prompt = buildPrompt(dto);
        return callOpenRouter(prompt);
    }

    private String buildPrompt(CoverLetterRequestDTO dto) {
        return "Write a professional cover letter for a software engineering job with the following details:\n\n" +
               "Job Title: " + dto.getJobTitle() + "\n" +
               "Company Name: " + dto.getCompanyName() + "\n" +
               "Applicant Skills: " + dto.getSkills() + "\n" +
               "Applicant Experience: " + dto.getExperience() + "\n" +
               "Job Description: " + dto.getJobDescription() + "\n" +
               "Tone: " + dto.getTone() + "\n\n" +
               "IMPORTANT RULES:\n" +
               "1. Start directly with 'Dear Hiring Manager,'\n" +
               "2. Write only the letter body — no subject line, no date, no address header\n" +
               "3. End with 'Sincerely,' followed by a blank line — do NOT add any name, phone, email or placeholder text after Sincerely\n" +
               "4. Do not add any bracketed placeholders like [Your Name] or [Your Email]\n" +
               "5. Keep it to 3-4 paragraphs, professional and concise";
    }

    private String callOpenRouter(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "http://localhost:8080");
        headers.set("X-Title", "Cover Letter Generator");

        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt
        );

        Map<String, Object> requestBody = Map.of(
        		"model", "nvidia/nemotron-3-ultra-550b-a55b:free",
                "messages", List.of(message),
                "max_tokens", 2000
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            String body = response.getBody();

            if (body == null) {
                return "Error: Empty response from AI service";
            }

            // Parse manually using simple string extraction
            // Find "content":"..." in the response
            String contentKey = "\"content\":\"";
            int startIndex = body.indexOf(contentKey);

            if (startIndex == -1) {
                return "Error: Could not find content in response: " + body.substring(0, Math.min(200, body.length()));
            }

            startIndex += contentKey.length();

            // Find the closing quote — accounting for escaped characters
            StringBuilder result = new StringBuilder();
            int i = startIndex;
            while (i < body.length()) {
                char c = body.charAt(i);
                if (c == '\\' && i + 1 < body.length()) {
                    char next = body.charAt(i + 1);
                    if (next == '"') {
                        result.append('"');
                        i += 2;
                    } else if (next == 'n') {
                        result.append('\n');
                        i += 2;
                    } else if (next == 't') {
                        result.append('\t');
                        i += 2;
                    } else if (next == '\\') {
                        result.append('\\');
                        i += 2;
                    } else {
                        result.append(c);
                        i++;
                    }
                } else if (c == '"') {
                    break;
                } else {
                    result.append(c);
                    i++;
                }
            }

            return result.toString().trim();

        } catch (Exception e) {
            return "Failed to generate. Please try again. Error: " + e.getMessage();
        }
    }    
    
    public String generateResumeSummary(String name, String skills, String experience, String targetRole) {
        String prompt = "Write a professional resume summary for the following candidate:\n\n" +
                "Name: " + name + "\n" +
                "Target Role: " + targetRole + "\n" +
                "Skills: " + skills + "\n" +
                "Experience: " + experience + "\n\n" +
                "RULES:\n" +
                "1. Write only the summary paragraph — 3 to 5 sentences maximum\n" +
                "2. Start directly with the candidate's professional identity\n" +
                "3. Highlight key skills and experience relevant to the target role\n" +
                "4. End with what value they bring to an employer\n" +
                "5. Do not include any labels, headings, or extra text";
        return callOpenRouter(prompt);
    }
    
    public String generateInterviewQuestions(String jobTitle, String skills) {
        String prompt = "Generate 10 technical interview questions for the following role:\n\n" +
                "Job Title: " + jobTitle + "\n" +
                "Skills Required: " + skills + "\n\n" +
                "RULES:\n" +
                "1. Write exactly 10 questions numbered 1 to 10\n" +
                "2. Mix conceptual questions and practical scenario-based questions\n" +
                "3. Focus on the skills listed\n" +
                "4. After each question add a brief answer hint in brackets\n" +
                "5. Do not add any intro or outro text";
        return callOpenRouter(prompt);
    }
    
}