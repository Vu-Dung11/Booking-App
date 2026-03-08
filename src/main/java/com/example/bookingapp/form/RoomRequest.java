package com.example.bookingapp.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class RoomRequest {
    @NotBlank(message = "Loại phòng không được để trống")
    private String roomType;

    @NotNull
    @Min(value = 1, message = "Sức chứa tối thiểu là 1")
    private Integer capacity;

    @NotNull
    @Min(value = 0, message = "Giá phòng không được âm")
    private BigDecimal basePrice;

    @NotNull
    @Min(value = 1, message = "Số lượng phòng tối thiểu là 1")
    private Integer quantity;
}
