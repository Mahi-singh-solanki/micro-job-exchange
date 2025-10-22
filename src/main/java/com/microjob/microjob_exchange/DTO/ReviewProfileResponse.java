package com.microjob.microjob_exchange.DTO;

import com.microjob.microjob_exchange.model.Review;
import lombok.Data;

import java.util.List;

@Data
public class ReviewProfileResponse {

    private Long userId;
    private Double averageRating;
    private long totalReviews;
    private List<Review> reviews; // List of all Review entities (including comments)
}