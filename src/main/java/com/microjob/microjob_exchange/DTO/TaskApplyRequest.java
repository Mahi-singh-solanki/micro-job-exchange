package com.microjob.microjob_exchange.DTO;

import lombok.Data;
import java.math.BigDecimal;

@Data // This annotation generates all necessary getters, setters, and constructors.
public class TaskApplyRequest {

    // Existing fields
    private String coverMessage;
    private BigDecimal proposedPrice;

    // NEW FIELDS (for Worker Contact Info)
    private String workerContactEmail;   // <-- Must be declared
    private String workerPhoneNumber;    // <-- Must be declared

    /*
     * NOTE: The manual getter and setter methods you provided in the original code
     * (e.g., getWorkerContactEmail, setWorkerContactEmail) should be DELETED.
     * Lombok's @Data handles their generation automatically.
     */
}