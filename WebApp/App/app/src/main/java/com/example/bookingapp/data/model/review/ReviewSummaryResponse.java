package com.example.bookingapp.data.model.review;

import java.util.Map;

public class ReviewSummaryResponse {
    private double averageRating;
    private long totalCount;
    private Map<Integer, Long> distribution;

    public double getAverageRating() { return averageRating; }
    public long getTotalCount() { return totalCount; }
    public Map<Integer, Long> getDistribution() { return distribution; }
}
