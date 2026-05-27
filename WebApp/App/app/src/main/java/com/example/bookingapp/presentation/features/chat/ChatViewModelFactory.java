package com.example.bookingapp.presentation.features.chat;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.data.remote.RetrofitClient;
import com.example.bookingapp.data.repository.ChatRepository;

public class ChatViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public ChatViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        ChatRepository repo = new ChatRepository(RetrofitClient.getChatApi(context));
        return (T) new ChatViewModel(repo);
    }
}
