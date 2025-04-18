package com.example.sparqlservice.filter;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.sparqlservice.config.SecurityConfig;
import com.example.sparqlservice.service.AuthenticationService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates API keys for non-public paths.
 * Simply passes through for public paths.
 *
 * @see AuthenticationService
 */
@Slf4j
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

  private final AuthenticationService authService;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();


  public ApiKeyAuthFilter(AuthenticationService authService) {
    this.authService = authService;
  }

  @SuppressWarnings("null")
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String requestPath = request.getRequestURI();

    log.debug("Request Path = {}", requestPath);

    if (SecurityConfig.PUBLIC_ENDPOINTS.stream().noneMatch(publicPath -> pathMatcher.match(publicPath, requestPath)) ) {
      // If the path is not public, check for the API key.
      try {
        Authentication approvedAuthentication = authService.getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(approvedAuthentication);

        log.debug("Passed non-Public endpoint {}", requestPath);
      } catch (BadCredentialsException e) {
        log.warn("Blocked non-Public endpoint {}", requestPath);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Unauthorized");
        return;
      }
    } else {
      // If the path is public, allow the request.
      log.debug("Passed Public endpoint {}", requestPath);
    }

    filterChain.doFilter(request, response);
  }
}
