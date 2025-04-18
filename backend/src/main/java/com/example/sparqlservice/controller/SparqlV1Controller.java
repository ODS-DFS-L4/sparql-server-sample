package com.example.sparqlservice.controller;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.sparqlservice.constants.RdfFormat;
import com.example.sparqlservice.constants.RdfMediaTypes;
import com.example.sparqlservice.service.SparqlService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "SPARQLエンドポイント", description = "SPARQLクエリを実行を行い、結果を返すエンドポイント。")
@Slf4j
@RestController
@RequestMapping("/api/v1/sparql")
@RequiredArgsConstructor
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Successful response"),
    @ApiResponse(responseCode = "400", description = "Bad request"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class SparqlV1Controller {

    private final SparqlService sparqlService;

    @GetMapping("")
    @Operation(summary = "ServiceDescriptionを取得する。", description = "指定されたフォーマットに基づいてServiceDescriptionを返す。")
    public ResponseEntity<String> getServiceDescription(
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String acceptHeader) {
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
    @Operation(summary = "POSTメソッドを使用してSPARQLクエリを実行する。", description = "SPARQLクエリをPOSTリクエストで実行し、指定されたフォーマットで結果を返す。")
    public ResponseEntity<String> executeSparqlQuery(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "SPARQL query to be executed.", required = true, content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/sparql-query", schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", example = "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10"))) @RequestBody String query,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String acceptHeader) {
        log.info("POST request:  query  = " + query);
        return executeQuery(query, acceptHeader);
    }

    @GetMapping("/query")
    @Operation(summary = "GETメソッドを使用してSPARQLクエリを実行する。", description = "SPARQLクエリをGETリクエストで実行し、指定されたフォーマットで結果を返す。")
    public ResponseEntity<String> executeSparqlQueryGet(
            @Parameter(description = "SPARQL query to be executed.", required = true, example = "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10") @RequestParam String query,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String acceptHeader) {
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
