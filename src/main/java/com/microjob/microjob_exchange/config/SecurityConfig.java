package com.microjob.microjob_exchange.config;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import com.microjob.microjob_exchange.security.OAuth2SuccessHandler;
import com.microjob.microjob_exchange.security.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
    //password encoder bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();

    }

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;


    @Autowired
    private JwtFilter jwtFilter;
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    //Security filter chaim
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public/Auth Endpoints
                        .requestMatchers("/auth/**", "/oauth2/**","/api/signin").permitAll()
                        // TASK MANAGEMENT ENDPOINTS
                        // POST /api/tasks: Only the uploader role can create a new task
                        .requestMatchers(HttpMethod.POST, "/api/tasks").hasAnyAuthority("ROLE_TASK_UPLOADER", "ROLE_WORKER","ROLE_ADMIN")
                        // GET /api/tasks & GET /api/tasks/{id}: Uploader and Worker can view
                        .requestMatchers(HttpMethod.GET, "/api/tasks", "/api/tasks/*").hasAnyAuthority("ROLE_TASK_UPLOADER", "ROLE_WORKER","ROLE_ADMIN")
                        // POST /api/tasks/{id}/apply: Only the worker role can apply
                        .requestMatchers(HttpMethod.POST, "/api/tasks/*/apply").hasAnyAuthority("ROLE_TASK_UPLOADER", "ROLE_WORKER","ROLE_ADMIN")
                        // Existing Role-Based Access
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/worker/**").hasRole("WORKER")
                        .requestMatchers("/uploader/**").hasRole("TASK_UPLOADER")
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN", "WORKER", "TASK_UPLOADER")
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/chat/**").authenticated()
                        // Fallback: Protect all other requests
                        .anyRequest().authenticated()
                )
                // Ensure you're setting the session management to stateless for JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // OAuth2 and JWT Filter setup (which you already have)
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173","http://127.0.0.1:3000"));
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Accept", "Cache-Control", "Content-Type", "Origin", "x-csrf-token", "x-requested-with"
        ));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}

