package com.example.bookingapp.data.model.chat;

import java.util.List;

/**
 * UI + transport model. Khi nhận từ server (GET /history) chỉ dùng id, role,
 * content, createdAt. Các field cards/typing/suggestions chỉ dùng phía client
 * để render UI và không gửi lên server.
 */
public class ChatMessage {

    public static final String ROLE_USER = "USER";
    public static final String ROLE_ASSISTANT = "ASSISTANT";
    public static final String ROLE_TOOL = "TOOL";

    private Long id;
    private String role;
    private String content;
    private String createdAt;

    // ====== client-only ======
    private transient boolean typing;
    private transient List<PropertyCard> cards;
    private transient List<String> suggestions;

    public ChatMessage() {}

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static ChatMessage typing() {
        ChatMessage m = new ChatMessage(ROLE_ASSISTANT, "");
        m.typing = true;
        return m;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isTyping() { return typing; }
    public void setTyping(boolean typing) { this.typing = typing; }

    public List<PropertyCard> getCards() { return cards; }
    public void setCards(List<PropertyCard> cards) { this.cards = cards; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public boolean isUser() { return ROLE_USER.equalsIgnoreCase(role); }
    public boolean isAssistant() { return ROLE_ASSISTANT.equalsIgnoreCase(role); }
    public boolean hasCards() { return cards != null && !cards.isEmpty(); }
}
