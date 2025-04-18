package com.example.sparqlservice.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

  @SuppressWarnings("null")
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    log.info("Request received from IP: {}, Port: {}, User-Agent: {}, Content-Type: {}, Request Path: {}, HTTP Method: {}",
             request.getHeader("X-Forwarded-For") != null ? request.getHeader("X-Forwarded-For") : request.getRemoteAddr(),
             request.getRemotePort(),
             ((HttpServletRequest) request).getHeader("User-Agent"),
             request.getContentType(),
             ((HttpServletRequest) request).getRequestURI(),
             ((HttpServletRequest) request).getMethod());
    filterChain.doFilter(request, response);
  }

}
