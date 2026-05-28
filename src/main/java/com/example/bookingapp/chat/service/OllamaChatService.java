package com.example.bookingapp.chat.service;

import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.configuration.exception.AppException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gọi Ollama REST API (http://localhost:11434/api/chat).
 * Hỗ trợ tool calling theo format OpenAI-compatible.
 */
@Slf4j
@Service
public class OllamaChatService {

    @Value("${ollama.base-url:https://ollama.com}")
    private String baseUrl;

    @Value("${ollama.api-key:}")
    private String apiKey;

    @Value("${ollama.model:gpt-oss:120b}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ─── Message types ────────────────────────────────────────────

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OllamaMessage {
        private String role;
        private String content;
        @JsonProperty("tool_calls")
        private List<OllamaToolCall> toolCalls;
        private String name;

        public static OllamaMessage system(String content) {
            OllamaMessage m = new OllamaMessage();
            m.role = "system";
            m.content = content;
            return m;
        }

        public static OllamaMessage user(String content) {
            OllamaMessage m = new OllamaMessage();
            m.role = "user";
            m.content = content;
            return m;
        }

        public static OllamaMessage assistant(String content) {
            OllamaMessage m = new OllamaMessage();
            m.role = "assistant";
            m.content = content;
            return m;
        }

        public static OllamaMessage assistantWithCalls(List<OllamaToolCall> calls) {
            OllamaMessage m = new OllamaMessage();
            m.role = "assistant";
            m.toolCalls = calls;
            return m;
        }

        public static OllamaMessage tool(String name, String content) {
            OllamaMessage m = new OllamaMessage();
            m.role = "tool";
            m.name = name;
            m.content = content;
            return m;
        }

        public boolean hasToolCalls() {
            return toolCalls != null && !toolCalls.isEmpty();
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OllamaToolCall {
        private OllamaFunction function;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OllamaFunction {
        private String name;
        @JsonDeserialize(using = ArgsDeserializer.class)
        private Map<String, Object> arguments;
    }

    // ─── Core generate method ─────────────────────────────────────

    public OllamaMessage generate(List<OllamaMessage> messages, List<Map<String, Object>> tools) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("stream", false);
        body.put("messages", messages);
        if (tools != null && !tools.isEmpty()) {
            body.put("tools", tools);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            headers.setBearerAuth(apiKey);
        }
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            log.debug("Ollama request model={} messages={}", model, messages.size());
            String json = restTemplate.postForObject(baseUrl + "/api/chat", entity, String.class);
            if (json == null) throw new AppException(ErrorCode.GEMINI_API_ERROR, "Empty response from Ollama");

            JsonNode root = objectMapper.readTree(json);
            JsonNode msgNode = root.get("message");
            if (msgNode == null) throw new AppException(ErrorCode.GEMINI_API_ERROR, "Missing 'message' in Ollama response");

            return objectMapper.treeToValue(msgNode, OllamaMessage.class);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ollama call failed: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.GEMINI_API_ERROR, "Ollama error: " + e.getMessage());
        }
    }

    // ─── Custom deserializer: arguments có thể là Map hoặc JSON string ───

    public static class ArgsDeserializer extends JsonDeserializer<Map<String, Object>> {
        private static final ObjectMapper MAPPER = new ObjectMapper();

        @Override
        public Map<String, Object> deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            if (node.isObject()) {
                return MAPPER.convertValue(node, new TypeReference<>() {});
            }
            if (node.isTextual()) {
                try {
                    return MAPPER.readValue(node.asText(), new TypeReference<>() {});
                } catch (Exception ignored) {}
            }
            return Map.of();
        }
    }
}
