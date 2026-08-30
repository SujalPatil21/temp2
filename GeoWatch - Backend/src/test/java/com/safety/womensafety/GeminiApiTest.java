package com.safety.womensafety;

import com.safety.womensafety.dto.GeminiAnalysisResult;
import com.safety.womensafety.service.GeminiRiskAnalysisService;
import org.springframework.test.util.ReflectionTestUtils;

public class GeminiApiTest {
    public static void main(String[] args) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("GEMINI_API_KEY environment variable is not set!");
            System.exit(1);
        }
        
        GeminiRiskAnalysisService service = new GeminiRiskAnalysisService();
        ReflectionTestUtils.setField(service, "geminiApiKey", apiKey);
        ReflectionTestUtils.setField(service, "geminiApiUrl", "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent");
        
        System.out.println("Calling Gemini API with description: 'Someone is carrying a weapon and attacking people near the entrance.'");
        GeminiAnalysisResult result = service.analyzeIncident("Someone is carrying a weapon and attacking people near the entrance.");
        
        if (result != null) {
            System.out.println("API Call Successful!");
            System.out.println("Semantic Risk: " + result.getSemanticRisk());
            System.out.println("Incident Type: " + result.getIncidentType());
            System.out.println("Reasoning: " + result.getReasoning());
        } else {
            System.err.println("API Call Failed or returned null.");
            System.exit(1);
        }
    }
}
