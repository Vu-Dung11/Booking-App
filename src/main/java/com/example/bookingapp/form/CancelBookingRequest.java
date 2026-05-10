package com.example.bookingapp.form;

import lombok.Getter;
import lombok.Setter;

/** Request body cho host huỷ booking. Optional reason. */
@Getter
@Setter
public class CancelBookingRequest {
    private String reason;
}
