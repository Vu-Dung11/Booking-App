package com.example.bookingapp.chat.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {
    @NotBlank(message = "sessionId không được để trống")
    private String sessionId;

    @NotBlank(message = "Tin nhắn không được để trống")
    private String message;

    private Long currentPropertyId;
}
