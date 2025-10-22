package com.microjob.microjob_exchange.service;

import com.microjob.microjob_exchange.model.PasswordResetToken;
import com.microjob.microjob_exchange.model.User;
import com.microjob.microjob_exchange.repository.PasswordResetTokenRepository;
import com.microjob.microjob_exchange.repository.UserRepository;
import com.microjob.microjob_exchange.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.SimpleMailMessage; // Required
import org.springframework.mail.javamail.JavaMailSender; // Required

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender; // CRITICAL: Inject the mail sender

    @Autowired
    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                JavaMailSender mailSender) { // Inject mailSender
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    // --- UTILITY METHODS ---

    private String generateOtp() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
    }

    // Method to construct and send the email
    private void sendEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@microjob.com");
        message.setTo(toEmail);
        message.setSubject("MicroJob Password Reset OTP");
        message.setText("Your password reset code is: " + otp + ". It expires in 5 minutes. Do not share this code.");

        mailSender.send(message); // Sends the email
    }

    // -------------------------------------------------------------------------
    // 1. Logic for POST /api/auth/request-otp (Generate & Send)
    // -------------------------------------------------------------------------
    @Transactional
    public void generateAndSendToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));

        // Generate new token and set expiry
        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        // Remove any old token for this user
        tokenRepository.findByUserEmail(email).ifPresent(tokenRepository::delete);

        // Create and save new token
        PasswordResetToken resetToken = new PasswordResetToken(email, otp, expiry);
        tokenRepository.save(resetToken);

        // Send email
        sendEmail(email, otp);
    }

    // -------------------------------------------------------------------------
    // 2. Logic for POST /api/auth/verify-otp (Validate)
    // -------------------------------------------------------------------------
    @Transactional
    public void validateOtp(String email, String code) {
        PasswordResetToken resetToken = tokenRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No active reset request found."));

        // Check if token has expired
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new SecurityException("Verification code has expired.");
        }

        // Check if token matches
        if (!resetToken.getToken().equals(code)) {
            throw new SecurityException("Invalid verification code.");
        }

        // Mark token as verified/used
        resetToken.setIsVerified(true);
        tokenRepository.save(resetToken);
    }

    // -------------------------------------------------------------------------
    // 3. Logic for POST /api/auth/reset-password (Final Reset)
    // -------------------------------------------------------------------------
    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        PasswordResetToken resetToken = tokenRepository.findByUserEmail(email)
                .orElseThrow(() -> new SecurityException("Verification required. Please request a code."));

        // Ensure the token was validated in the previous step
        if (!resetToken.getIsVerified()) {
            throw new SecurityException("Code verification has not been completed.");
        }

        // Update the password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Clean up used token
        tokenRepository.delete(resetToken);
    }
}