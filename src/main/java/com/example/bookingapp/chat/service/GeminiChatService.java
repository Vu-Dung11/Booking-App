package com.example.bookingapp.chat.service;

import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.configuration.exception.AppException;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Wrap Google GenAI SDK. Cung cấp 2 thao tác chính:
 *  - generate(history, config) trả về response thô từ Gemini
 *  - các helper build user message / function response part
 */
@Slf4j
@Service
public class GeminiChatService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    private Client client;

    @PostConstruct
    void init() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("gemini.api.key is empty. Chat API will fail until GEMINI_API_KEY env is set.");
            return;
        }
        this.client = Client.builder().apiKey(apiKey).build();
    }

    @PreDestroy
    void close() {
        if (client != null) {
            client.close();
        }
    }

    public String getModel() {
        return model;
    }

    public GenerateContentResponse generate(List<Content> history, GenerateContentConfig config) {
        if (client == null) {
            throw new AppException(ErrorCode.GEMINI_API_ERROR, "API key chưa được set");
        }
        try {
            return client.models.generateContent(model, history, config);
        } catch (Exception e) {
            log.error("Gemini generateContent failed: {}", e.getMessage(), e);
            String detail = e.getClass().getSimpleName() + ": " + e.getMessage();
            throw new AppException(ErrorCode.GEMINI_API_ERROR, detail);
        }
    }

    // ============================================================
    // helpers — build Content

    public static Content userText(String text) {
        return Content.builder().role("user").parts(List.of(Part.fromText(text))).build();
    }

    public static Content modelText(String text) {
        return Content.builder().role("model").parts(List.of(Part.fromText(text))).build();
    }

    public static Content modelFunctionCall(FunctionCall fc) {
        Part p = Part.builder().functionCall(fc).build();
        return Content.builder().role("model").parts(List.of(p)).build();
    }

    public static Content functionResponse(String name, java.util.Map<String, Object> response) {
        return Content.builder()
                .role("user")
                .parts(List.of(Part.fromFunctionResponse(name, response)))
                .build();
    }
}
