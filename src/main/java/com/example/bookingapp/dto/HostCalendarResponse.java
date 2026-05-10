package com.example.bookingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Calendar view của 1 property: list rooms × list ngày trong khoảng [from, to].
 * bookedCount derived từ Booking table; availableCount lấy từ inventory row.
 * hasInventory=false nếu chưa có row (chưa mở lịch ngày đó).
 */
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class HostCalendarResponse {
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<RoomCalendar> rooms;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RoomCalendar {
        private Long roomId;
        private String roomType;
        private Integer capacity;
        private Integer quantity;
        private List<DayInventory> days;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DayInventory {
        private LocalDate date;
        private Integer availableCount;
        private Integer bookedCount;
        private Boolean hasInventory;
    }
}
