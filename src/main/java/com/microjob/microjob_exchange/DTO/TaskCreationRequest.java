package com.microjob.microjob_exchange.DTO;

import lombok.Data;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


@Data // Lombok annotation
public class TaskCreationRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be a positive value")
    private BigDecimal price;

    @NotNull(message = "Latitude is required for matching")
    private Double latitude;

    @NotNull(message = "Longitude is required for matching")
    private Double longitude;

    @Positive(message = "Duration must be positive and in minutes")
    private Integer durationMinutes;

    @NotBlank(message = "Contact email is required for the poster.")
    private String contactEmail; // <-- NEW FIELD

    @NotBlank(message = "Phone number is required for the poster.")
    private String contactPhoneNumber; //
}
