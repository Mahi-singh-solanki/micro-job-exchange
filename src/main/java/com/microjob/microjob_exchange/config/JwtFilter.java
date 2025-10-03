package com.microjob.microjob_exchange.config;

import com.microjob.microjob_exchange.service.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // <-- NEW IMPORT
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.util.List;
import java.io.IOException;
import java.util.Collections; // <-- NEW IMPORT
@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    // REMOVE @Autowired private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Handle Preflight/Options Request
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        String jwtToken = null;
        String email = null;

        // --- START CRITICAL PATH TRY-CATCH ---
        // This block will catch silent failures (e.g., token parsing, expired token)
        try {

            // 2. Extract Token
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwtToken = authHeader.substring(7);

                // 3. Extract Email (to be used as the principal)
                email = jwtUtil.extractEmail(jwtToken);
            }

            // 4. VALIDATE TOKEN AND SET CONTEXT STATELSSLY
            // Only proceed if email is present and no authentication exists in the context
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtUtil.validateToken(jwtToken)) {

                    // 5. Extract Role and Create Authorities
                    String role = jwtUtil.extractRole(jwtToken);

                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority(role) // Uses the full prefixed role
                    );

                    // 6. Create and Set Authentication Token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            email, // Principal
                            null,  // Credentials
                            authorities // Authorities (Roles)
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    System.out.println("DEBUG: Successfully authenticated user: " + email + " with role: " + role);
                } else {
                    // Token invalid (e.g., expired) - This is the silent failure point
                    System.err.println("DEBUG: Token validation FAILED for user: " + email);
                }
            }
        } catch (Exception e) {
            // CATCH ALL: This will print the exception for any parsing, signature, or claim error.
            System.err.println("CRITICAL JWT PROCESSING ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            // You can uncomment the line below to immediately tell the client the token failed.
            // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authentication Token");

        }
        // --- END CRITICAL PATH TRY-CATCH ---

        // 7. Continue with next filter
        filterChain.doFilter(request, response);
    }
}