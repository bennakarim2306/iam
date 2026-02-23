// src/main/java/com/hop/drivesharing/hopapplication/config/SecurityConfig.java
package com.foodopia.backend.config;

import com.foodopia.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz ->
                        authz
                                .requestMatchers("/api/v1/auth/**").permitAll()
                                .requestMatchers("GET", "/api/v1/items/**").permitAll() // Allow browsing items without authentication
                                .requestMatchers("POST", "/api/v1/items/**").authenticated() // Require authentication for creating items
                                .requestMatchers("PUT", "/api/v1/items/**").authenticated() // Require authentication for updating items
                                .requestMatchers("DELETE", "/api/v1/items/**").authenticated() // Require authentication for deleting items
                                .requestMatchers("/api/v1/account/**").authenticated() // Account operations require authentication
                                .requestMatchers("/actuator/**", "/management/**").permitAll()
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }
}
