package com.safety.womensafety.controller;

import com.safety.womensafety.dto.ChatRequest;
import com.safety.womensafety.dto.ChatResponse;
import com.safety.womensafety.service.GeminiChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/chat")
public class ChatbotController {

    private final GeminiChatbotService chatbotService;

    public ChatbotController(GeminiChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.getEventId() == null || request.getMessage() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        String lang = request.getLanguage() != null && !request.getLanguage().isEmpty() 
                        ? request.getLanguage() : "English";

        String answer = chatbotService.chatWithGemini(request.getEventId(), request.getMessage(), lang);
        
        return ResponseEntity.ok(new ChatResponse(answer));
    }
}
