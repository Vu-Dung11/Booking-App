package com.example.bookingapp.chat.service;

import com.example.bookingapp.chat.dto.ChatMessageResponse;
import com.example.bookingapp.chat.dto.PropertyCard;
import com.example.bookingapp.chat.entity.ChatMessage;
import com.example.bookingapp.chat.entity.ChatRole;
import com.example.bookingapp.chat.entity.ChatSession;
import com.example.bookingapp.chat.form.ChatMessageRequest;
import com.example.bookingapp.chat.repository.ChatMessageRepository;
import com.example.bookingapp.chat.repository.ChatSessionRepository;
import com.example.bookingapp.chat.service.OllamaChatService.OllamaMessage;
import com.example.bookingapp.chat.service.OllamaChatService.OllamaToolCall;
import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.configuration.utils.SecurityUtils;
import com.example.bookingapp.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final OllamaChatService ollama;
    private final ChatToolRegistry toolRegistry;
    private final ChatToolExecutor toolExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${chat.history.max-messages:20}")
    private int maxHistory;

    private static final int MAX_TOOL_ITERATIONS = 5;

    public ChatService(ChatSessionRepository sessionRepository,
                       ChatMessageRepository messageRepository,
                       SecurityUtils securityUtils,
                       OllamaChatService ollama,
                       ChatToolRegistry toolRegistry,
                       ChatToolExecutor toolExecutor) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.securityUtils = securityUtils;
        this.ollama = ollama;
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
    }

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
        messageRepository.save(ChatMessage.builder()
                .session(session)
                .role(ChatRole.USER)
                .content(req.getMessage())
                .build());

        // 2. Build messages: system + history
        List<OllamaMessage> messages = new ArrayList<>();
        messages.add(OllamaMessage.system(buildSystemPrompt()));
        messages.addAll(buildHistory(session, req));

        List<Map<String, Object>> tools = toolRegistry.buildTools();
        List<PropertyCard> aggregatedCards = new ArrayList<>();
        String finalReply = null;

        // 3. Tool-call loop (tối đa MAX_TOOL_ITERATIONS vòng)
        for (int iter = 0; iter < MAX_TOOL_ITERATIONS; iter++) {
            OllamaMessage resp = ollama.generate(messages, tools);

            if (!resp.hasToolCalls()) {
                finalReply = resp.getContent();
                break;
            }

            // Thêm assistant message (có tool_calls) vào history
            messages.add(OllamaChatService.OllamaMessage.assistantWithCalls(resp.getToolCalls()));

            // Thực thi từng tool call
            for (OllamaToolCall tc : resp.getToolCalls()) {
                String fname = tc.getFunction().getName();
                Map<String, Object> fargs = tc.getFunction().getArguments();
                log.info("Ollama tool call: {} args={}", fname, fargs);

                ChatToolExecutor.ToolResult tr = toolExecutor.execute(fname, fargs);
                if (tr.cards() != null) aggregatedCards.addAll(tr.cards());

                // Lưu TOOL message để audit
                try {
                    messageRepository.save(ChatMessage.builder()
                            .session(session)
                            .role(ChatRole.TOOL)
                            .content(objectMapper.writeValueAsString(tr.response()))
                            .toolName(fname)
                            .toolArgs(objectMapper.writeValueAsString(fargs))
                            .build());
                } catch (JsonProcessingException ignored) {}

                // Thêm tool result vào messages
                try {
                    messages.add(OllamaMessage.tool(fname, objectMapper.writeValueAsString(tr.response())));
                } catch (JsonProcessingException e) {
                    messages.add(OllamaMessage.tool(fname, "{}"));
                }
            }
        }

        if (finalReply == null || finalReply.isBlank()) {
            finalReply = "Xin lỗi, mình chưa thể trả lời câu hỏi này. Bạn thử diễn đạt lại nhé.";
        }

        // 4. Lưu ASSISTANT reply, cập nhật session
        messageRepository.save(ChatMessage.builder()
                .session(session)
                .role(ChatRole.ASSISTANT)
                .content(finalReply)
                .build());

        session.setLastActiveAt(LocalDateTime.now());
        if ("Cuộc trò chuyện mới".equals(session.getTitle())) {
            session.setTitle(shorten(req.getMessage(), 60));
        }
        sessionRepository.save(session);

        return ChatMessageResponse.builder()
                .reply(finalReply)
                .suggestions(buildSuggestions(finalReply))
                .cards(aggregatedCards.isEmpty() ? null : aggregatedCards)
                .build();
    }

    // ─────────────────────────────────────────────────────────────

    private ChatSession requireOwnedSession(String sessionId) {
        ChatSession s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAT_SESSION_NOT_FOUND));
        Long uid = securityUtils.getCurrentUser().getId();
        if (s.getUser() == null || !uid.equals(s.getUser().getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return s;
    }

    private List<OllamaMessage> buildHistory(ChatSession session, ChatMessageRequest req) {
        var msgs = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        var filtered = msgs.stream().filter(m -> m.getRole() != ChatRole.TOOL).toList();
        int start = Math.max(0, filtered.size() - maxHistory);
        filtered = filtered.subList(start, filtered.size());

        List<OllamaMessage> out = new ArrayList<>();
        for (ChatMessage m : filtered) {
            if (m.getRole() == ChatRole.USER) {
                out.add(OllamaMessage.user(m.getContent()));
            } else {
                out.add(OllamaMessage.assistant(m.getContent()));
            }
        }

        // Inject context hint khi user đang xem màn hình chi tiết property
        if (req.getCurrentPropertyId() != null && !out.isEmpty()) {
            OllamaMessage last = out.get(out.size() - 1);
            if ("user".equals(last.getRole())) {
                String hint = "[Bối cảnh: user đang xem propertyId=" + req.getCurrentPropertyId() + "]";
                out.set(out.size() - 1, OllamaMessage.user(hint + "\n" + last.getContent()));
            }
        }
        return out;
    }

    private String buildSystemPrompt() {
        String today = LocalDate.now().toString();
        return "Bạn là trợ lý ảo của BookingApp — nền tảng đặt homestay. "
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
    }

    private List<String> buildSuggestions(String reply) {
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
