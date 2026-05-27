package com.example.bookingapp.chat.service;

import com.example.bookingapp.chat.dto.ChatMessageResponse;
import com.example.bookingapp.chat.dto.PropertyCard;
import com.example.bookingapp.chat.entity.ChatMessage;
import com.example.bookingapp.chat.entity.ChatRole;
import com.example.bookingapp.chat.entity.ChatSession;
import com.example.bookingapp.chat.form.ChatMessageRequest;
import com.example.bookingapp.chat.repository.ChatMessageRepository;
import com.example.bookingapp.chat.repository.ChatSessionRepository;
import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.configuration.utils.SecurityUtils;
import com.example.bookingapp.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.Content;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final SecurityUtils securityUtils;
    private final GeminiChatService gemini;
    private final ChatToolRegistry toolRegistry;
    private final ChatToolExecutor toolExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${chat.history.max-messages:20}")
    private int maxHistory;

    public ChatService(ChatSessionRepository sessionRepository,
                       ChatMessageRepository messageRepository,
                       SecurityUtils securityUtils,
                       GeminiChatService gemini,
                       ChatToolRegistry toolRegistry,
                       ChatToolExecutor toolExecutor) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.securityUtils = securityUtils;
        this.gemini = gemini;
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
    }

    private static final int MAX_TOOL_ITERATIONS = 5;

    @Transactional
    public String createSession() {
        User user = securityUtils.getCurrentUser();
        ChatSession s = ChatSession.builder()
                .user(user)
                .title("Cuộc trò chuyện mới")
                .build();
        return sessionRepository.save(s).getId();
    }

    @Transactional(readOnly = true)
    public List<ChatSession> getMySessions() {
        Long uid = securityUtils.getCurrentUser().getId();
        return sessionRepository.findByUserIdOrderByLastActiveAtDesc(uid);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getHistory(String sessionId) {
        ChatSession session = requireOwnedSession(sessionId);
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
    }

    @Transactional
    public ChatMessageResponse handleMessage(ChatMessageRequest req) {
        ChatSession session = requireOwnedSession(req.getSessionId());

        // 1. Save USER message
        ChatMessage userMsg = ChatMessage.builder()
                .session(session)
                .role(ChatRole.USER)
                .content(req.getMessage())
                .build();
        messageRepository.save(userMsg);

        // 2. Build history for Gemini
        List<Content> history = buildHistory(session, req);

        // 3. Call Gemini with tools, handle function-call loop
        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(buildSystemInstruction())
                .tools(toolRegistry.buildTools())
                .temperature(0.7f)
                .build();

        List<PropertyCard> aggregatedCards = new ArrayList<>();
        String finalReply = null;

        for (int iter = 0; iter < MAX_TOOL_ITERATIONS; iter++) {
            GenerateContentResponse resp = gemini.generate(history, config);
            List<FunctionCall> calls = safeFunctionCalls(resp);

            if (calls.isEmpty()) {
                finalReply = safeText(resp);
                break;
            }

            // Add model's function call(s) to history
            List<Part> callParts = new ArrayList<>();
            for (FunctionCall fc : calls) {
                callParts.add(Part.builder().functionCall(fc).build());
            }
            history.add(Content.builder().role("model").parts(callParts).build());

            // Execute each tool, append function responses
            List<Part> respParts = new ArrayList<>();
            for (FunctionCall fc : calls) {
                String fname = fc.name().orElse("");
                Map<String, Object> fargs = fc.args().orElse(Map.of());
                log.info("Gemini tool call: {} args={}", fname, fargs);

                ChatToolExecutor.ToolResult tr = toolExecutor.execute(fname, fargs);
                if (tr.cards() != null) {
                    aggregatedCards.addAll(tr.cards());
                }

                // Save TOOL message for audit
                try {
                    ChatMessage toolMsg = ChatMessage.builder()
                            .session(session)
                            .role(ChatRole.TOOL)
                            .content(objectMapper.writeValueAsString(tr.response()))
                            .toolName(fname)
                            .toolArgs(objectMapper.writeValueAsString(fargs))
                            .build();
                    messageRepository.save(toolMsg);
                } catch (JsonProcessingException ignored) {
                }

                respParts.add(Part.fromFunctionResponse(fname, tr.response()));
            }
            history.add(Content.builder().role("user").parts(respParts).build());
        }

        if (finalReply == null || finalReply.isBlank()) {
            finalReply = "Xin lỗi, mình chưa thể trả lời câu hỏi này. Bạn thử diễn đạt lại nhé.";
        }

        // 4. Save ASSISTANT reply, bump session
        messageRepository.save(ChatMessage.builder()
                .session(session)
                .role(ChatRole.ASSISTANT)
                .content(finalReply)
                .build());

        session.setLastActiveAt(LocalDateTime.now());
        if (session.getTitle() == null || "Cuộc trò chuyện mới".equals(session.getTitle())) {
            session.setTitle(shorten(req.getMessage(), 60));
        }
        sessionRepository.save(session);

        return ChatMessageResponse.builder()
                .reply(finalReply)
                .suggestions(buildSuggestions(finalReply))
                .cards(aggregatedCards.isEmpty() ? null : aggregatedCards)
                .build();
    }

    // ============================================================

    private ChatSession requireOwnedSession(String sessionId) {
        ChatSession s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAT_SESSION_NOT_FOUND));
        Long uid = securityUtils.getCurrentUser().getId();
        if (s.getUser() == null || !uid.equals(s.getUser().getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return s;
    }

    private List<Content> buildHistory(ChatSession session, ChatMessageRequest req) {
        var msgs = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        // Trim: chỉ giữ N message gần nhất, bỏ qua TOOL (đã có context trong reply)
        var filtered = msgs.stream()
                .filter(m -> m.getRole() != ChatRole.TOOL)
                .toList();
        int start = Math.max(0, filtered.size() - maxHistory);
        filtered = filtered.subList(start, filtered.size());

        List<Content> out = new ArrayList<>();
        for (ChatMessage m : filtered) {
            String role = m.getRole() == ChatRole.USER ? "user" : "model";
            out.add(Content.builder().role(role).parts(List.of(Part.fromText(m.getContent()))).build());
        }

        // Inject context-aware hint if user opened chat from a property detail screen
        if (req.getCurrentPropertyId() != null && !out.isEmpty()) {
            Content last = out.get(out.size() - 1);
            if ("user".equals(last.role().orElse(""))) {
                String hint = "[Bối cảnh: user đang xem propertyId=" + req.getCurrentPropertyId() + "]";
                out.set(out.size() - 1, Content.builder().role("user")
                        .parts(List.of(Part.fromText(hint + "\n" + m_safeText(last)))).build());
            }
        }
        return out;
    }

    private static String m_safeText(Content c) {
        return c.parts().orElse(List.of()).stream()
                .map(p -> p.text().orElse(""))
                .reduce("", (a, b) -> a + b);
    }

    private Content buildSystemInstruction() {
        String today = LocalDate.now().toString();
        String prompt = "Bạn là trợ lý ảo của BookingApp — nền tảng đặt homestay. "
                + "Trả lời ngắn gọn, thân thiện, bằng tiếng Việt. Hôm nay là " + today + ".\n\n"
                + "QUY TẮC QUAN TRỌNG:\n"
                + "1. TUYỆT ĐỐI KHÔNG HỎI user về propertyId, roomId, bookingId — họ là khách, "
                + "không biết các ID này. ID phải được lấy từ kết quả tool trước đó hoặc từ "
                + "thông tin '[Bối cảnh: ...]' nếu có.\n"
                + "2. Khi user muốn tìm phòng → hỏi đủ city + ngày nhận + ngày trả + số khách → "
                + "gọi searchProperties. Hiển thị kết quả dưới dạng danh sách (tên + thành phố + giá).\n"
                + "3. Khi user muốn xem chi tiết một homestay (ví dụ 'cái đầu tiên', 'homestay X') → "
                + "tự suy ra propertyId từ kết quả searchProperties gần nhất và gọi getPropertyDetail. "
                + "Khi user nói 'ở đây' / 'nơi này' / 'homestay này' và có [Bối cảnh: propertyId=X] → "
                + "dùng propertyId đó.\n"
                + "4. Khi user muốn đặt phòng → đảm bảo đã có roomId từ getPropertyDetail trước đó. "
                + "Trước khi gọi createBooking PHẢI xác nhận lại bằng câu hỏi yes/no: tên phòng, "
                + "ngày nhận, ngày trả, số phòng, tổng tiền (nếu biết).\n"
                + "5. Khi user hỏi booking của họ → gọi getMyBookings (không cần arg) hoặc "
                + "getBookingDetail(bookingId) với bookingId từ getMyBookings.\n"
                + "6. Format giá tiền dạng VND (vd: 1.200.000đ). Format ngày dd/MM/yyyy khi nói với user, "
                + "nhưng yyyy-MM-dd khi gọi tool.";
        return Content.builder().parts(List.of(Part.fromText(prompt))).build();
    }

    private List<FunctionCall> safeFunctionCalls(GenerateContentResponse resp) {
        try {
            var calls = resp.functionCalls();
            return calls == null ? List.of() : new ArrayList<>(calls);
        } catch (Exception e) {
            return List.of();
        }
    }

    private String safeText(GenerateContentResponse resp) {
        try {
            return resp.text();
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> buildSuggestions(String reply) {
        // Static suggestions — có thể nâng cấp sau (ví dụ gọi Gemini gen quick replies)
        String low = reply == null ? "" : reply.toLowerCase();
        if (low.contains("homestay") || low.contains("phòng")) {
            return List.of("Xem chi tiết", "Đặt phòng luôn", "Tìm chỗ khác");
        }
        if (low.contains("booking") || low.contains("đặt phòng")) {
            return List.of("Xem booking của tôi", "Huỷ đặt phòng", "Hướng dẫn thanh toán");
        }
        return List.of("Tìm homestay", "Xem booking của tôi", "Hướng dẫn thanh toán");
    }

    private static String shorten(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
