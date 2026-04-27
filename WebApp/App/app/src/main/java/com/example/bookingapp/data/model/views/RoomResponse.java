package com.example.bookingapp.data.model.views;

import java.math.BigDecimal;

public class RoomResponse {
    private Long roomId;
    private String roomType;
    private BigDecimal price;
    private Integer capacity;


    public Long getRoomId() {
        return roomId;
    }

    public String getRoomType() {
        return roomType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getCapacity() {
        return capacity;
    }
}
