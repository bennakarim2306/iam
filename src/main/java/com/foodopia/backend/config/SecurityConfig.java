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
                                // Authentication endpoints - public
                                .requestMatchers("/api/v1/auth/**").permitAll()

                                // Items - GET allowed without auth, POST/PUT/DELETE require auth
                                .requestMatchers("GET", "/api/v1/items/**").permitAll()
                                .requestMatchers("POST", "/api/v1/items/**").authenticated()
                                .requestMatchers("PUT", "/api/v1/items/**").authenticated()
                                .requestMatchers("DELETE", "/api/v1/items/**").authenticated()

                                // Transactions - ALL operations require authentication
                                .requestMatchers("/api/v1/transactions/**").authenticated()

                                // Account operations require authentication
                                .requestMatchers("/api/v1/account/**").authenticated()

                                // OpenAPI / Swagger documentation - public
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/swagger-ui.html").permitAll()

                                // Actuator endpoints - public
                                .requestMatchers("/actuator/**", "/management/**").permitAll()

                                // Default: all other requests require authentication
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }
}
