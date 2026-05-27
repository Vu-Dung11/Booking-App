package com.example.bookingapp.data.model.chat;

public class ChatRequest {
    private String sessionId;
    private String message;
    private Long currentPropertyId;

    public ChatRequest(String sessionId, String message, Long currentPropertyId) {
        this.sessionId = sessionId;
        this.message = message;
        this.currentPropertyId = currentPropertyId;
    }

    public String getSessionId() { return sessionId; }
    public String getMessage() { return message; }
    public Long getCurrentPropertyId() { return currentPropertyId; }
}
