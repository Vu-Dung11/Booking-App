package com.example.bookingapp.data.model.chat;

import java.util.List;

public class ChatResponse {
    private String reply;
    private List<String> suggestions;
    private List<PropertyCard> cards;

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public List<PropertyCard> getCards() { return cards; }
    public void setCards(List<PropertyCard> cards) { this.cards = cards; }
}
