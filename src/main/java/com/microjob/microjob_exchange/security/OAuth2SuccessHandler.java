package com.microjob.microjob_exchange.security;

import com.microjob.microjob_exchange.model.User;
import com.microjob.microjob_exchange.repository.UserRepository;
import com.microjob.microjob_exchange.service.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

// CRITICAL FIX: Extend SimpleUrlAuthenticationSuccessHandler to gain redirect functionality
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // Inject dependencies through constructor
    @Autowired
    public OAuth2SuccessHandler(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // --- 1. USER PROVISIONING/LOOKUP ---
        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;

        if (optionalUser.isEmpty()) {
            // Create new user if they don't exist
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setRole("TASK_UPLOADER"); // Default role
            userRepository.save(user);
        } else {
            user = optionalUser.get();
        }

        // 2. Generate Application JWT
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        // --- 3. CRITICAL: PERFORM REDIRECT WITH TOKEN ---

        // **URL TO CHANGE:** Replace 'http://localhost:3000' with your actual frontend address (e.g., http://127.0.0.1:5173)
        // This is the frontend route that will capture the JWT token from the URL
        String frontendCallbackUrl = "http://localhost:5173/oauth/callback?token=" + token;

        // Use the built-in redirect strategy (HTTP 302)
        getRedirectStrategy().sendRedirect(request, response, frontendCallbackUrl);
    }
}