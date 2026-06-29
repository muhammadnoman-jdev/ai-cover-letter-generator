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
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            
            // DEBUG - print full response to console
            System.out.println("=== OPENROUTER RESPONSE ===");
            System.out.println(response.getBody());
            System.out.println("===========================");

            Map<String, Object> body = response.getBody();
            
            if (body == null) {
                return "Error: Empty response from AI service";
            }

            // Check for error field in response
            if (body.containsKey("error")) {
                return "AI Error: " + body.get("error").toString();
            }

            List<Map> choices = (List<Map>) body.get("choices");
            
            if (choices == null || choices.isEmpty()) {
                return "Error: No choices in response. Full response: " + body.toString();
            }
            
            Map firstChoice = choices.get(0);
            Map messageResponse = (Map) firstChoice.get("message");
            return (String) messageResponse.get("content");

        } catch (Exception e) {
            return "Failed to generate cover letter. Please try again. Error: " + e.getMessage();
        }
    }
}