package com.microjob.microjob_exchange.security;

import com.microjob.microjob_exchange.model.User;
import com.microjob.microjob_exchange.repository.UserRepository;
import com.microjob.microjob_exchange.service.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;

        if (optionalUser.isEmpty()) {
            // Create new user with default role
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setRole("TASK_UPLOADER");
            userRepository.save(user);
        } else {
            user = optionalUser.get();
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        // Write token directly to response
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"Login successful\", \"token\": \"" + token + "\"}");
        response.getWriter().flush();
    }
}
