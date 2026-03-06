package com.example.bookingapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

// TRẢ VỀ CÁC THÔNG TIN MÀ DEV MUỐN, 
// Map lại, giúp giữ các thông tin không được lộ ra như mật khẩu

@Getter
@Setter
public class PostDTO {

    private UUID id;
    private String title;
    private String description;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;

}
