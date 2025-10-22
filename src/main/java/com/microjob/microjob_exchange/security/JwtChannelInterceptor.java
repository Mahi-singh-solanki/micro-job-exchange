package com.microjob.microjob_exchange.security;

import com.microjob.microjob_exchange.service.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String token = null;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            // 1. ATTEMPT: Extract from native 'Authorization' header (standard approach)
            List<String> authorizationHeaders = accessor.getNativeHeader(AUTH_HEADER);

            if (authorizationHeaders != null && !authorizationHeaders.isEmpty()) {
                String authHeaderValue = authorizationHeaders.get(0);
                if (authHeaderValue.startsWith(BEARER_PREFIX)) {
                    token = authHeaderValue.substring(BEARER_PREFIX.length()).trim();
                }
            }

            // 2. Fallback check (for simple browser clients sending token in query/custom header)
            if (token == null || token.isEmpty()) {
                List<String> queryTokenList = accessor.getNativeHeader("token");
                if (queryTokenList != null && !queryTokenList.isEmpty()) {
                    // Assuming token is sent without "Bearer" in the query string
                    token = queryTokenList.get(0).trim();
                }
            }


            if (token != null && !token.isEmpty() && jwtUtil.validateToken(token)) {

                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token); // e.g., ROLE_WORKER

                if (StringUtils.hasText(email) && StringUtils.hasText(role)) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority(role))
                    );
                    // CRITICAL: Set the authenticated principal for the session
                    accessor.setUser(auth);
                }
            } else if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                // Reject connection if token is missing or invalid
                throw new SecurityException("JWT Token missing or invalid for WebSocket handshake.");
            }
        }
        return message;
    }
}