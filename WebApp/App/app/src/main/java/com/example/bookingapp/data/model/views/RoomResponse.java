package com.example.bookingapp.data.model.views;

import java.math.BigDecimal;

public class RoomResponse {
    private Long roomId;
    private String roomType;
    private BigDecimal price;
    private Integer capacity;
    private Integer quantity;
    private String thumbnailUrl;
    /** Số phòng còn trống tối thiểu trong khoảng [checkIn, checkOut); null nếu endpoint không truyền date. */
    private Integer availableCount;


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

    public Integer getQuantity() {
        return quantity;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Integer getAvailableCount() {
        return availableCount;
    }
}
