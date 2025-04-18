package com.example.sparqlservice.service.externalApi;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalAPIService {

  private final WebClient webClient;

  /**
   * This method is currently unused.
   *
   * Asynchronous method for sending HTTP requests.
   *
   * @param <T>
   * @param <R>
   * @param baseUrl
   * @param pathAndQuery
   * @param method
   * @param requestBody
   * @param responseClass
   * @return
   */
  @SuppressWarnings("unused")
	public <T, R> Mono<R> callExternalApi(String baseUrl, String pathAndQuery, HttpMethod method, T requestBody,
      Class<R> responseClass) {
    log.debug("Calling external API - baseUrl: {}, path: {}, method: {}, responseClass: {}",
        baseUrl, pathAndQuery, method, responseClass.getSimpleName());

    if (baseUrl == null || baseUrl.trim().isEmpty()) {
      log.error("Base URL cannot be null or empty");
      return Mono.error(new IllegalArgumentException("Base URL cannot be null or empty"));
    }
    if (pathAndQuery == null || pathAndQuery.trim().isEmpty()) {
      log.error("Path and query cannot be null or empty");
      return Mono.error(new IllegalArgumentException("Path and query cannot be null or empty"));
    }
    if (method == null) {
      log.error("HTTP method cannot be null");
      return Mono.error(new IllegalArgumentException("HTTP method cannot be null"));
    }
    if (responseClass == null) {
      log.error("Response class cannot be null");
      return Mono.error(new IllegalArgumentException("Response class cannot be null"));
    }

    WebClient.RequestBodySpec requestSpec = webClient.method(method)
        .uri(baseUrl + pathAndQuery);

    // When request body is provided, add it to the request
    // This handles POST/PUT requests that need to send data
    if (requestBody != null) {
      log.debug("Adding request body of type: {}", requestBody.getClass().getSimpleName());
      requestSpec.body(Mono.just(requestBody), requestBody.getClass());
    }

    return requestSpec.retrieve()
        .onStatus(statusCode -> statusCode.is4xxClientError(), response -> {
          log.error("Client error: {}", response.statusCode());
          return Mono.error(new HttpClientErrorException(response.statusCode()));
        })
        .onStatus(statusCode -> statusCode.is5xxServerError(), response -> {
          log.error("Server error: {}", response.statusCode());
          return Mono.error(new HttpServerErrorException(response.statusCode()));
        })
        .bodyToMono(responseClass)
        .timeout(Duration.ofSeconds(5))
        .doOnError(error -> log.error("Error calling external API", error))
        .onErrorMap(TimeoutException.class,
            ex -> {
              log.error("API call timed out after 5 seconds");
              return new ResourceAccessException("API call timed out");
            })
        .onErrorResume(error -> {
          log.warn("Falling back to empty response due to error", error);
          return Mono.empty();
        })
        .doOnSuccess(response -> log.debug("Successfully received response of type: {}",
            responseClass.getSimpleName()));
  }

  /**
   * Synchronous method for sending HTTP requests.
   *
   * @param <T>
   * @param <R>
   * @param baseUrl
   * @param pathAndQuery
   * @param method
   * @param requestBody
   * @param responseClass
   * @return
   */
  @SuppressWarnings("unused")
	public <T, R> R callExternalApiSync(String baseUrl, String pathAndQuery, HttpMethod method, T requestBody,
      Class<R> responseClass) {
    log.debug("Calling external API synchronously - baseUrl: {}, path: {}, method: {}, responseClass: {}",
        baseUrl, pathAndQuery, method, responseClass.getSimpleName());

    if (baseUrl == null || baseUrl.trim().isEmpty()) {
      log.error("Base URL cannot be null or empty");
      throw new IllegalArgumentException("Base URL cannot be null or empty");
    }
    if (pathAndQuery == null || pathAndQuery.trim().isEmpty()) {
      log.error("Path and query cannot be null or empty");
      throw new IllegalArgumentException("Path and query cannot be null or empty");
    }
    if (method == null) {
      log.error("HTTP method cannot be null");
      throw new IllegalArgumentException("HTTP method cannot be null");
    }
    if (responseClass == null) {
      log.error("Response class cannot be null");
      throw new IllegalArgumentException("Response class cannot be null");
    }

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
    ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(5000);
    ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(5000);

    try {
      HttpHeaders headers = new HttpHeaders();
      HttpEntity<T> requestEntity = new HttpEntity<>(requestBody, headers);

      ResponseEntity<R> response = restTemplate.exchange(
          baseUrl + pathAndQuery,
          method,
          requestEntity,
          responseClass);

      log.debug("Successfully received response of type: {}, body: {}", responseClass.getSimpleName(), response.getBody());
      return response.getBody();

    } catch (HttpClientErrorException e) {
      log.error("Client error: {}", e.getStatusCode());
      return null;
    } catch (HttpServerErrorException e) {
      log.error("Server error: {}", e.getStatusCode());
      return null;
    } catch (ResourceAccessException e) {
      log.error("API call timed out or connection error", e);
      return null;
    } catch (Exception e) {
      log.error("Error calling external API", e);
      return null;
    }
  }
}