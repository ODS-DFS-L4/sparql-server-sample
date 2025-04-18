package com.example.sparqlservice.service;

import org.springframework.stereotype.Service;

import com.example.sparqlservice.constants.RdfFormat;
import com.example.sparqlservice.repository.GeneralRdfRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SparqlService {

    private final GeneralRdfRepository rdfRepository;

    public SparqlService(GeneralRdfRepository rdfRepository) {
        this.rdfRepository = rdfRepository;
    }

    /**
     * Retrieves the service description in the specified format.
     *
     * @param format the specified RdfFormat
     * @return the service description as a string in the specified RdfFormat
     */
    public String getServiceDescription(RdfFormat format) {
        try {
            return rdfRepository.getServiceDescription(format);
        } catch (Exception e) {
            log.error("Error retrieving service description in format {}: {}", format, e.getMessage());
            throw e;
        }
    }

    /**
     * Executes a query and returns the result in the specified format.
     *
     * @param queryString the SPARQL query to be executed
     * @param format the specified RdfFormat
     * @return the result of the SPARQL query as a string in the specified RdfFormat
     */
    public String executeQuery(String queryString, RdfFormat format) {
        try {
            return rdfRepository.executeQuery(queryString, format);
        } catch (Exception e) {
            log.error("Error executing SPARQL query: {}", queryString);
            throw e;
        }
    }
}
