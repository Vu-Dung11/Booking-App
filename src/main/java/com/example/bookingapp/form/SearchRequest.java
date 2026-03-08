package com.example.bookingapp.form;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SearchRequest {
    @NotBlank(message = "Ngày nhận phòng không hợp lệ")
    private String city;

    @FutureOrPresent(message = "Ngày nhận không hợp lệ")
    private LocalDate checkIn;

    @Future(message = "Ngày trả phòng phải sau ngày hiện tại")
    private LocalDate checkOut;

    @Min(value = 1, message = "số lượng khách tối thiểu là 1")
    private Integer guests;

}
