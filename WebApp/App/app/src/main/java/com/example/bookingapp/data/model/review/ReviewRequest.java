package com.example.bookingapp.data.model.review;

public class ReviewRequest {
    private Long bookingId;
    private Integer rating;
    private String comment;

    public ReviewRequest(Long bookingId, Integer rating, String comment) {
        this.bookingId = bookingId;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getBookingId() { return bookingId; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
}
