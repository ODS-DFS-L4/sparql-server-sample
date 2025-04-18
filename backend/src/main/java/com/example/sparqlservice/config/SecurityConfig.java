package com.example.sparqlservice.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.sparqlservice.filter.ApiKeyAuthFilter;
import com.example.sparqlservice.filter.RequestLoggingFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final ApiKeyAuthFilter apiKeyAuthFilter;
  private final RequestLoggingFilter requestLoggingFilter;

  /*
   * Public endpoints where authentication is not needed.
   */
  public static final List<String> PUBLIC_ENDPOINTS = List.of(
      "/error",
      "/api/metadata/**",
      "/api/sparql/**",
      "/api/v1/sparql/**",
      "/api/model/**",
      "/v3/api-docs/**",
      "/v3/api-docs.yaml",
      "/swagger-ui/**",
      "/favicon.ico");

  /**
   * Configures the security filter chain for the application.
   *
   * @param http the HttpSecurity object to configure
   * @return the configured SecurityFilterChain
   * @throws Exception if an error occurs while configuring the security
   */
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_ENDPOINTS.toArray(String[]::new)).permitAll()
            .anyRequest().authenticated())
        // Replace UsernamePasswordAuthenticationFilter with ApiKeyAuthFilter
        .addFilterAt(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(requestLoggingFilter, ApiKeyAuthFilter.class)
        .formLogin(login -> login.disable())
        .logout(logout -> logout.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }

  /**
   * Manually create this class to prevent Spring Security auto-configuration from
   * generating a security password.
   *
   * @return
   */
  @Bean
  public UserDetailsService userDetailsService() {
    return new InMemoryUserDetailsManager();
  }
}
