package com.example.bookingapp.form;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookingRequest {
    @NotNull(message = "Vui lòng chọn phòng")
    private Long roomId;

    @FutureOrPresent(message = "Ngày nhận phòng không hợp lệ")
    private LocalDate checkIn;

    @Future(message = "Ngày trả phòng phải sau ngày hiện tại")
    private LocalDate checkOut;

    @Min(value = 1, message = "Số lượng phòng đặt tối thiểu là 1")
    private Integer roomQuantity; // Số lượng phòng muốn đặt (VD: đặt 2 phòng Deluxe)
}