package com.example.bookingapp.form;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Mở thêm inventory đến untilDate. Backend sẽ tạo rows cho mọi ngày từ
 * MAX(inventory_date)+1 đến untilDate, mỗi row availableCount = room.quantity.
 * Idempotent: skip ngày đã tồn tại.
 */
@Getter
@Setter
public class ExtendInventoryRequest {
    @NotNull
    private LocalDate untilDate;
}
