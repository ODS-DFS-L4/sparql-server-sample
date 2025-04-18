package com.example.sparqlservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import com.example.sparqlservice.auth.ApiKeyAuthentication;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthenticationService {
  private final String AUTH_HEADER;
  private final String API_KEY;

  AuthenticationService(@Value("${auth.header}") String authHeader, @Value("${auth.apiKey}") String apiKey) {
    this.AUTH_HEADER = authHeader;
    this.API_KEY = apiKey;
  }

  /**
   * Retrieves the authentication object based on the provided HTTP request.
   *
   * This method checks the request for a valid API key in the specified authentication header.
   * If the API key is missing or invalid, a {@link BadCredentialsException} is thrown.
   * If the API key is valid, an {@link ApiKeyAuthentication} object is created and returned.
   *
   * @param request the HTTP request containing the API key in the header
   * @return an {@link Authentication} object representing the authenticated user
   * @throws BadCredentialsException if the API key is missing or invalid
   */
  public Authentication getAuthentication(HttpServletRequest request) throws BadCredentialsException {
    String apiKey = request.getHeader(AUTH_HEADER);
    if (apiKey == null || !apiKey.equals(API_KEY)) {
      throw new BadCredentialsException("Invalid API Key");
    }

    log.debug("Successfully Authenticated");
    return new ApiKeyAuthentication(apiKey, AuthorityUtils.NO_AUTHORITIES);
  }
}
