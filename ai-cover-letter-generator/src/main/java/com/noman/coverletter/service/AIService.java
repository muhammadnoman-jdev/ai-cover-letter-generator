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
        return "Write a professional cover letter with the following details:\n\n" +
               "Job Title: " + dto.getJobTitle() + "\n" +
               "Company Name: " + dto.getCompanyName() + "\n" +
               "Applicant Skills: " + dto.getSkills() + "\n" +
               "Applicant Experience: " + dto.getExperience() + "\n" +
               "Job Description: " + dto.getJobDescription() + "\n" +
               "Tone: " + dto.getTone() + "\n\n" +
               "Write only the cover letter text. No explanations. No subject line. " +
               "Start directly with 'Dear Hiring Manager,' and end with a proper sign-off.";
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

            List<Map> choices = (List<Map>) response.getBody().get("choices");
            Map firstChoice = choices.get(0);
            Map messageResponse = (Map) firstChoice.get("message");
            return (String) messageResponse.get("content");

        } catch (Exception e) {
            return "Failed to generate cover letter. Please try again. Error: " + e.getMessage();
        }
    }
}