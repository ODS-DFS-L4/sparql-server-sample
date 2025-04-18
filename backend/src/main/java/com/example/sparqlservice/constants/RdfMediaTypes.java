package com.example.sparqlservice.constants;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;

/**
 * Defines a mapping between various RDF media types
 * and their corresponding RdfFormat enums.
 */
public class RdfMediaTypes {

  public static final Map<MediaType, RdfFormat> SUPPORTED_TYPES;

  // Multiple MediaTypeConstants value can be assigned to the same RdfFormat.
  static {
    SUPPORTED_TYPES = new LinkedHashMap<>();
    SUPPORTED_TYPES.put(MediaTypeConstants.APPLICATION_SPARQL_RESULTS_JSON, RdfFormat.JSON);
    SUPPORTED_TYPES.put(MediaTypeConstants.APPLICATION_SPARQL_RESULTS_XML, RdfFormat.RDFXML);
    SUPPORTED_TYPES.put(MediaTypeConstants.TEXT_TURTLE, RdfFormat.TURTLE);
    SUPPORTED_TYPES.put(MediaTypeConstants.APPLICATION_N_TRIPLES, RdfFormat.NTRIPLES);
    SUPPORTED_TYPES.put(MediaTypeConstants.APPLICATION_JSON, RdfFormat.JSON);
    SUPPORTED_TYPES.put(MediaTypeConstants.APPLICATION_LD_JSON, RdfFormat.JSONLD);
    SUPPORTED_TYPES.put(MediaTypeConstants.APPLICATION_RDF_XML, RdfFormat.RDFXML);
  }

  public class MediaTypeConstants {
    public static final String APPLICATION_RDF_XML_STRING = "application/rdf+xml";
    public static final String TEXT_TURTLE_STRING = "text/turtle";
    public static final String APPLICATION_N_TRIPLES_STRING = "application/n-triples";
    public static final String APPLICATION_JSON_STRING = MediaType.APPLICATION_JSON_VALUE;
    public static final String APPLICATION_LD_JSON_STRING = "application/ld+json";
    public static final String APPLICATION_SPARQL_RESULTS_JSON_STRING = "application/sparql-results+json";
    public static final String APPLICATION_SPARQL_RESULTS_XML_STRING = "application/sparql-results+xml";

    public static final MediaType APPLICATION_RDF_XML = MediaType.parseMediaType(APPLICATION_RDF_XML_STRING);
    public static final MediaType TEXT_TURTLE = MediaType.parseMediaType(TEXT_TURTLE_STRING);
    public static final MediaType APPLICATION_N_TRIPLES = MediaType.parseMediaType(APPLICATION_N_TRIPLES_STRING);
    public static final MediaType APPLICATION_JSON = MediaType.parseMediaType(APPLICATION_JSON_STRING);
    public static final MediaType APPLICATION_LD_JSON = MediaType.parseMediaType(APPLICATION_LD_JSON_STRING);
    public static final MediaType APPLICATION_SPARQL_RESULTS_JSON = MediaType
        .parseMediaType(APPLICATION_SPARQL_RESULTS_JSON_STRING);
    public static final MediaType APPLICATION_SPARQL_RESULTS_XML = MediaType
        .parseMediaType(APPLICATION_SPARQL_RESULTS_XML_STRING);
  }

}
