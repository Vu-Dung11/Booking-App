package com.example.bookingapp.presentation.features.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.chat.ChatMessage;
import com.example.bookingapp.data.model.chat.ChatRequest;
import com.example.bookingapp.data.model.chat.ChatResponse;
import com.example.bookingapp.data.repository.ChatRepository;

import java.util.List;

public class ChatViewModel extends ViewModel {
    private final ChatRepository repository;
    private final MutableLiveData<Resource<String>> sessionState = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<ChatMessage>>> historyState = new MutableLiveData<>();
    private final MutableLiveData<Resource<ChatResponse>> sendState = new MutableLiveData<>();

    public ChatViewModel(ChatRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<String>> getSessionState() { return sessionState; }
    public LiveData<Resource<List<ChatMessage>>> getHistoryState() { return historyState; }
    public LiveData<Resource<ChatResponse>> getSendState() { return sendState; }

    public void createSession() {
        repository.createSession(sessionState);
    }

    public void loadHistory(String sessionId) {
        repository.getHistory(sessionId, historyState);
    }

    public void sendMessage(String sessionId, String message, Long currentPropertyId) {
        repository.sendMessage(new ChatRequest(sessionId, message, currentPropertyId), sendState);
    }
}
