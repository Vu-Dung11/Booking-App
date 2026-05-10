package com.example.bookingapp.form;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Bulk update inventory: set availableCount cho tất cả ngày trong [fromDate, toDate].
 * fromDate <= toDate. Range không quá 90 ngày để tránh DDoS.
 */
@Getter
@Setter
public class DayInventoryUpdateRequest {
    @NotNull
    private LocalDate fromDate;
    @NotNull
    private LocalDate toDate;
    @NotNull
    private Integer availableCount;
}
