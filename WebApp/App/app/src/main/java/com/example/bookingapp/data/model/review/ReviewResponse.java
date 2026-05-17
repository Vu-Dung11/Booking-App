package com.example.bookingapp.data.model.review;

public class ReviewResponse {
    private Long id;
    private Long propertyId;
    private String propertyName;
    private Long bookingId;
    private String guestName;
    private Integer rating;
    private String comment;
    private String createdAt;

    public Long getId() { return id; }
    public Long getPropertyId() { return propertyId; }
    public String getPropertyName() { return propertyName; }
    public Long getBookingId() { return bookingId; }
    public String getGuestName() { return guestName; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public String getCreatedAt() { return createdAt; }
}
