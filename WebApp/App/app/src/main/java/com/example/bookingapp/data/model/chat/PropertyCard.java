package com.example.bookingapp.data.model.chat;

import java.math.BigDecimal;

public class PropertyCard {
    private Long propertyId;
    private String name;
    private String thumbnailUrl;
    private String city;
    private BigDecimal minPrice;

    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
}
