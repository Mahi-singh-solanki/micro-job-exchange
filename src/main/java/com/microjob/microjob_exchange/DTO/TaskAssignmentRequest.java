package com.microjob.microjob_exchange.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class TaskAssignmentRequest {

    @NotNull(message = "Application ID is required for assignment")
    private Long applicationId;

    private String workerContactEmail;   // <-- NEW FIELD
    private String workerPhoneNumber;

    // Optional: Could include a final agreed price if negotiation occurred
    // private BigDecimal finalPrice; 
}