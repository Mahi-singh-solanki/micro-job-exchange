package com.microjob.microjob_exchange.controller;

import com.microjob.microjob_exchange.model.User;
import com.microjob.microjob_exchange.service.JwtUtil;
import com.microjob.microjob_exchange.repository.UserRepository;
import com.microjob.microjob_exchange.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<?> login(@RequestBody User loginRequest)
    {
        //Authentication
        Authentication authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        //generate JWT token
        String token =jwtUtil.generateToken(loginRequest.getId(),loginRequest.getEmail(),loginRequest.getRole());
        return ResponseEntity.ok("Bearer "+token);

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
    }

