package com.example.bookingapp.data.model.views;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class PropertyResponse {
    @SerializedName("id")
    private Long propertyId;

    @SerializedName("name")
    private String propertyName;

    private String address;
    private String city;
    private BigDecimal minPrice;

    public Long getPropertyId() { return propertyId; }
    public String getPropertyName() { return propertyName; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public BigDecimal getMinPrice() { return minPrice; }
}
