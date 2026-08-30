package com.safety.womensafety.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safety.womensafety.dto.GeminiAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiRiskAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(GeminiRiskAnalysisService.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent}")
    private String geminiApiUrl;

    public GeminiRiskAnalysisService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public GeminiAnalysisResult analyzeIncident(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null; // Should not happen given outer logic, but safe fallback
        }

        try {
            String url = geminiApiUrl + "?key=" + geminiApiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = "You are a crowd-safety semantic risk classifier. " +
                    "Classify the following incident description based on the immediate threat to life or safety. " +
                    "Categories: " +
                    "HIGH: active violence, weapons, severe medical emergency, fire, explosion, dangerous crowd compression/crush, immediate threat to life/safety. " +
                    "MEDIUM: harassment, aggressive behavior, suspicious activity, minor medical assistance, severe overcrowding without immediate injury. " +
                    "LOW: lost items, directions, noise complaint, general assistance, minor inconvenience, or vague/insufficient descriptions like 'help' or 'idk'. " +
                    "RULES: " +
                    "1. Classify based ONLY on the provided description. Do NOT invent facts. " +
                    "2. If context is insufficient, default to LOW. " +
                    "3. Normal inconvenience should not be escalated. " +
                    "4. Return valid JSON ONLY. No markdown wrapping. " +
                    "JSON FORMAT: {\"semanticRisk\": \"LOW|MEDIUM|HIGH\", \"incidentType\": \"string\", \"reasoning\": \"string\"} " +
                    "Description: " + description;

            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            
            Map<String, Object> content = new HashMap<>();
            content.put("parts", Collections.singletonList(part));
            
            requestBody.put("contents", Collections.singletonList(content));
            
            // Set JSON response format if Gemini supports it in payload, or we rely on prompt instruction
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("response_mime_type", "application/json");
            requestBody.put("generationConfig", generationConfig);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode candidates = rootNode.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode parts = candidates.get(0).path("content").path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        String jsonResponseText = parts.get(0).path("text").asText();
                        // Parse the inner JSON
                        GeminiAnalysisResult result = objectMapper.readValue(jsonResponseText, GeminiAnalysisResult.class);
                        
                        // Validate enum
                        if (!"LOW".equals(result.getSemanticRisk()) && 
                            !"MEDIUM".equals(result.getSemanticRisk()) && 
                            !"HIGH".equals(result.getSemanticRisk())) {
                            log.warn("Invalid semanticRisk from Gemini: {}", result.getSemanticRisk());
                            return null;
                        }
                        
                        return result;
                    }
                }
            }
            
            log.warn("Gemini API did not return expected structure. Response code: {}", response.getStatusCode());
            return null;

        } catch (Exception e) {
            log.error("Error communicating with Gemini API", e);
            // DO NOT set to LOW. Return null so spatial risk is used
            return null;
        }
    }
}
