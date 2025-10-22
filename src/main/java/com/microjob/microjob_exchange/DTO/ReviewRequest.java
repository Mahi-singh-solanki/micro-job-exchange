package com.microjob.microjob_exchange.DTO;

import com.microjob.microjob_exchange.model.Review.ReviewType;
import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class ReviewRequest {

    @NotNull
    @Min(1) @Max(5)
    private Integer rating;

    @NotBlank
    private String comments;

    @NotNull
    private ReviewType reviewType; // POSTER_TO_WORKER or WORKER_TO_POSTER
}