package com.microjob.microjob_exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false, unique = true)
    private String userEmail; // Linked to the user's email

    @Column(nullable = false)
    private String token; // The generated 6-digit code or UUID

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false; // Flag to check if OTP was successfully entered

    // Utility constructor for service
    public PasswordResetToken(String userEmail, String token, LocalDateTime expiryDate) {
        this.userEmail = userEmail;
        this.token = token;
        this.expiryDate = expiryDate;
        this.isVerified = false;
    }
}