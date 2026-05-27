package com.example.bookingapp.chat.controller;

import com.example.bookingapp.chat.dto.ChatMessageResponse;
import com.example.bookingapp.chat.entity.ChatMessage;
import com.example.bookingapp.chat.entity.ChatSession;
import com.example.bookingapp.chat.form.ChatMessageRequest;
import com.example.bookingapp.chat.service.ChatService;
import com.example.bookingapp.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/session")
    public ApiResponse<Map<String, String>> createSession() {
        String id = chatService.createSession();
        return ApiResponse.success(Map.of("sessionId", id));
    }

    @PostMapping("/message")
    public ApiResponse<ChatMessageResponse> sendMessage(@Valid @RequestBody ChatMessageRequest request) {
        return ApiResponse.success(chatService.handleMessage(request));
    }

    @GetMapping("/history")
    public ApiResponse<List<ChatMessage>> getHistory(@RequestParam String sessionId) {
        return ApiResponse.success(chatService.getHistory(sessionId));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<ChatSession>> getSessions() {
        return ApiResponse.success(chatService.getMySessions());
    }
}
