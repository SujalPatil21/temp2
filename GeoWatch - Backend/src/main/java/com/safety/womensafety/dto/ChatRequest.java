package com.safety.womensafety.dto;

public class ChatRequest {
    private Long eventId;
    private String message;
    private String language;

    public ChatRequest() {}

    public ChatRequest(Long eventId, String message, String language) {
        this.eventId = eventId;
        this.message = message;
        this.language = language;
    }

    public Long getEventId() {
        return eventId;
    }
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
}
