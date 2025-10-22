package com.microjob.microjob_exchange.controller;

import com.microjob.microjob_exchange.model.PasswordResetToken;
import com.microjob.microjob_exchange.service.PasswordResetService;
import com.microjob.microjob_exchange.model.User;
import com.microjob.microjob_exchange.service.JwtUtil;
import com.microjob.microjob_exchange.repository.UserRepository;
import com.microjob.microjob_exchange.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
//@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;


    @Autowired
    private PasswordResetService passwordResetService;
    //signup
    @PostMapping("/auth/signup")
    public ResponseEntity<?> registerUser(@RequestBody User userRequest){
        //checking email
        if(userRepository.findByEmail(userRequest.getEmail()).isPresent()){
            return ResponseEntity.badRequest().body("Email already in use");
        }

        //Encode password
        userRequest.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        //Default role as Task uploader
        if(userRequest.getRole()==null||userRequest.getRole().isEmpty()){
            userRequest.setRole("TASK_UPLOADER");
        }

        //save user
        userRepository.save(userRequest);
        return ResponseEntity.ok("User registered succesfully");
    }

    //login
    @PostMapping("/api/signin")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        // 1. Authentication Check
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. RETRIEVE FULL USER OBJECT from DB using the authenticated email
        String authenticatedEmail = authentication.getName(); // Use the authenticated principal's name/email

        Optional<User> userOptional = userRepository.findByEmail(authenticatedEmail);

        if (!userOptional.isPresent()) {
            // This should ideally never happen after successful authentication
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User data not found after authentication.");
        }

        User user = userOptional.get();
        System.out.println("DEBUG FINAL CHECK: User ID being packaged in JWT: " + user.getId());

        // 3. GENERATE JWT TOKEN using the retrieved user data
        String token = jwtUtil.generateToken(
                user.getId(),    // Correct ID
                user.getEmail(), // Correct Email
                user.getRole()   // Correct Role (e.g., "TASK_UPLOADER")
        );

        return ResponseEntity.ok("Bearer " + token);
    }
    //current user

    @GetMapping("/auth/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader){
        if(authHeader==null||!authHeader.startsWith("Bearer ")) {
            return  ResponseEntity.status(401).body("No token provided");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }
        String email = jwtUtil.extractEmail(token);

        Optional<User> user=userRepository.findByEmail(email);
        return user.<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(()->ResponseEntity.status(404).body("User not found"));

        }
    @PostMapping("/auth/request-otp")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        passwordResetService.generateAndSendToken(email);
        return ResponseEntity.ok().body("Verification code sent to email.");
    }

    @PostMapping("/auth/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        passwordResetService.validateOtp(email, code);
        return ResponseEntity.ok().body("Code verified successfully.");
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");
        passwordResetService.resetPassword(email, newPassword);
        return ResponseEntity.ok().body("Password reset successful. Please log in.");
    }
    }


