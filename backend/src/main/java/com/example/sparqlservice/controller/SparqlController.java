package com.example.sparqlservice.controller;

import com.example.sparqlservice.constants.RdfFormat;
import com.example.sparqlservice.constants.RdfMediaTypes;
import com.example.sparqlservice.service.SparqlService;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * This class was initially used, but later versioned paths like /v1 started being used,
 * hence it is now deprecated.
 * @deprecated
 */
@Deprecated
@Hidden // This controller shouldn't be on OpenAPI specification
@Slf4j
@RestController
@RequestMapping("/api/sparql")
@RequiredArgsConstructor
public class SparqlController {

    private final SparqlService sparqlService;

    @GetMapping("")
    public ResponseEntity<String> getServiceDescription(
            @RequestHeader(value = HttpHeaders.ACCEPT, defaultValue = "application/ld+json") String acceptHeader) {
        try {
            MediaType selectedMediaType = findRdfFormatByAcceptHeader(acceptHeader);
            RdfFormat selectedFormat = RdfMediaTypes.SUPPORTED_TYPES.get(selectedMediaType);

            String description = sparqlService.getServiceDescription(selectedFormat);
            return ResponseEntity.ok()
                    .contentType(selectedMediaType)
                    .body(description);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error retrieving service description: " + e.getMessage());
        }
    }

    @PostMapping("/query")
    public ResponseEntity<String> executeSparqlQuery(
            @RequestBody String query,
            @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader) {
        log.info("POST request:  query  = " + query);
        return executeQuery(query, acceptHeader);
    }

    @GetMapping("/query")
    public ResponseEntity<String> executeSparqlQueryGet(
            @RequestParam String query,
            @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader) {
        log.info("GET request: query = " + query);
        return executeQuery(query, acceptHeader);
    }

    private ResponseEntity<String> executeQuery(String query, String acceptHeader) {
        if (query.startsWith("query=")) {
            query = query.substring(6);
        }

        try {
            query = URLDecoder.decode(query, "UTF-8");

            // Get the appropriate MediaType and RdfFormat from Accept Header
            MediaType selectedMediaType = findRdfFormatByAcceptHeader(acceptHeader);
            RdfFormat selectedFormat = RdfMediaTypes.SUPPORTED_TYPES.get(selectedMediaType);

            log.info("Selected format: {}, mediaType: {}", selectedFormat, selectedMediaType);

            String result = sparqlService.executeQuery(query, selectedFormat);
            return ResponseEntity.ok()
                    .contentType(selectedMediaType)
                    .body(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error executing query: " + e.getMessage());
        }
    }

    /**
     * Determines the appropriate MediaType based on the client's Accept Header.
     *
     * @param acceptHeader the value of the Accept header from the client's request
     * @return the corresponding MediaType for the accepted media type, or JSON-LD
     *         as the default
     */
    private MediaType findRdfFormatByAcceptHeader(String acceptHeader) {
        log.info("Received Accept header: {}", acceptHeader);

        // Parse Accept header into list of MediaType objects
        List<MediaType> acceptableMediaTypes = MediaType.parseMediaTypes(acceptHeader);

        // Supported Types in this application
        Map<MediaType, RdfFormat> supportedTypes = RdfMediaTypes.SUPPORTED_TYPES;
        MediaType defaultMediaType = RdfMediaTypes.MediaTypeConstants.APPLICATION_LD_JSON;

        // Find the best matching media type
        MediaType selectedMediaType = acceptableMediaTypes.stream()
                .flatMap(acceptedType -> supportedTypes.keySet().stream()
                        .filter(supportedType -> acceptedType.isCompatibleWith(supportedType)))
                .findFirst()
                .orElseGet(() -> {
                    log.warn("No compatible MediaType found, using default: {}", defaultMediaType);
                    return defaultMediaType;
                });

        return selectedMediaType;
    }
}
