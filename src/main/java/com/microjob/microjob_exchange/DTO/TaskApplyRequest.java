package com.microjob.microjob_exchange.DTO;

import lombok.Data;
import java.math.BigDecimal;

@Data // Lombok annotation
public class TaskApplyRequest {
    private String coverMessage;

    // In case the user can propose a different price (bidding)
    private BigDecimal proposedPrice;
}
