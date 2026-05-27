package com.example.bookingapp.data.remote;

import com.example.bookingapp.data.model.ApiResponse;
import com.example.bookingapp.data.model.chat.ChatMessage;
import com.example.bookingapp.data.model.chat.ChatRequest;
import com.example.bookingapp.data.model.chat.ChatResponse;
import com.example.bookingapp.data.model.chat.ChatSessionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ChatApi {
    @POST("api/v1/chat/session")
    Call<ApiResponse<ChatSessionResponse>> createSession();

    @POST("api/v1/chat/message")
    Call<ApiResponse<ChatResponse>> sendMessage(@Body ChatRequest request);

    @GET("api/v1/chat/history")
    Call<ApiResponse<List<ChatMessage>>> getHistory(@Query("sessionId") String sessionId);

    @GET("api/v1/chat/sessions")
    Call<ApiResponse<List<Object>>> getSessions();
}
