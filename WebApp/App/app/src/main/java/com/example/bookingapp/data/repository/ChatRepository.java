package com.example.bookingapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.ApiResponse;
import com.example.bookingapp.data.model.chat.ChatMessage;
import com.example.bookingapp.data.model.chat.ChatRequest;
import com.example.bookingapp.data.model.chat.ChatResponse;
import com.example.bookingapp.data.model.chat.ChatSessionResponse;
import com.example.bookingapp.data.remote.ChatApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {
    private final ChatApi api;

    public ChatRepository(ChatApi api) {
        this.api = api;
    }

    public void createSession(MutableLiveData<Resource<String>> state) {
        state.setValue(Resource.loading());
        api.createSession().enqueue(new Callback<ApiResponse<ChatSessionResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ChatSessionResponse>> call,
                                   Response<ApiResponse<ChatSessionResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    state.setValue(Resource.success(response.body().getData().getSessionId()));
                } else {
                    state.setValue(Resource.error("Không tạo được session: " + response.code(), null));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<ChatSessionResponse>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }

    public void sendMessage(ChatRequest request, MutableLiveData<Resource<ChatResponse>> state) {
        state.setValue(Resource.loading());
        api.sendMessage(request).enqueue(new Callback<ApiResponse<ChatResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ChatResponse>> call,
                                   Response<ApiResponse<ChatResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ChatResponse> body = response.body();
                    if (body.getData() != null) {
                        state.setValue(Resource.success(body.getData()));
                    } else {
                        state.setValue(Resource.error(
                                body.getMessage() == null ? "Lỗi không xác định" : body.getMessage(), null));
                    }
                } else {
                    state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<ChatResponse>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }

    public void getHistory(String sessionId, MutableLiveData<Resource<List<ChatMessage>>> state) {
        state.setValue(Resource.loading());
        api.getHistory(sessionId).enqueue(new Callback<ApiResponse<List<ChatMessage>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChatMessage>>> call,
                                   Response<ApiResponse<List<ChatMessage>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    state.setValue(Resource.success(response.body().getData()));
                } else {
                    state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<ChatMessage>>> call, Throwable t) {
                state.setValue(Resource.error(t.getLocalizedMessage(), null));
            }
        });
    }
}
