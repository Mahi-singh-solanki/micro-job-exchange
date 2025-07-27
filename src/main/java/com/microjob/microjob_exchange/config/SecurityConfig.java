package com.microjob.microjob_exchange.config;

import com.microjob.microjob_exchange.security.OAuth2SuccessHandler;
import com.microjob.microjob_exchange.security.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    //password encoder bean
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    //Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception{
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.csrf(csrf-> csrf.disable()).authorizeHttpRequests(auth->auth.requestMatchers("/auth/**","/oauth2/**","/api/signin").permitAll().requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/worker/**").hasRole("WORKER").requestMatchers("/uploader/**").hasRole("TASK_UPLOADER").requestMatchers("/user/**").hasAnyRole("USER", "ADMIN", "WORKER", "TASK_UPLOADER").anyRequest().authenticated()).oauth2Login(oauth->oauth.userInfoEndpoint(userInfo->userInfo.userService(customOAuth2UserService)).successHandler(oAuth2SuccessHandler)).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).formLogin(form->form.disable()).httpBasic(httpBasic->httpBasic.disable());
        return http.build();
    }
}
