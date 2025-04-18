package com.example.sparqlservice.repository;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.sparqlservice.annotation.UpdateModifiedAt;
import com.example.sparqlservice.constants.RdfFormat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GeneralRdfRepository {
  private final String fusekiResourceEndpoint;
  private final String serverUrl; // The URL of this server as seen from the internet

  public GeneralRdfRepository(@Value("${fuseki.resource-endpoint}") String fusekiResourceEndpoint,
      @Value("${server.url}") String serverUrl) {
    this.fusekiResourceEndpoint = fusekiResourceEndpoint;
    this.serverUrl = serverUrl;
  }

  /**
   * Executes queries (SELECT, CONSTRUCT, ASK, DESCRIBE) and converts each result
   * into a String object
   * according to the specified RdfFormat object, then returns it.
   *
   * @param queryStr
   * @param format
   * @return
   */
  public String executeQuery(String queryString, RdfFormat format) {

    Query query = QueryFactory.create(queryString);

    try (QueryExecutionHTTP qexec = QueryExecutionHTTP.service(fusekiResourceEndpoint, queryString)) {
      log.info("Executing query of type: {}", query.queryType());
      switch (query.queryType()) {
        case SELECT:
          log.debug("Executing SELECT query");
          ResultSet results = qexec.execSelect();
          log.debug("SELECT query executed successfully");
          return formatSelectResults(results, format);
        case CONSTRUCT:
          log.debug("Executing CONSTRUCT query");
          Model constructModel = qexec.execConstruct();
          log.debug("CONSTRUCT query executed successfully");
          return formatModel(constructModel, format);
        case ASK:
          log.debug("Executing ASK query");
          boolean askResult = qexec.execAsk();
          log.debug("ASK query executed successfully with result: {}", askResult);
          return formatAskResult(askResult, format);
        case DESCRIBE:
          log.debug("Executing DESCRIBE query");
          Model describeModel = qexec.execDescribe();
          log.debug("DESCRIBE query executed successfully");
          return formatModel(describeModel, format);
        default:
          log.warn("Unknown query type: {}", query.queryType());
          throw new IllegalArgumentException("Unsupported query type: " + query.queryType());
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      throw e;
    }
  }

  /**
   * Save the RDF model to the Jena triple store.
   *
   * @param model the RDF model to be saved
   */
  @UpdateModifiedAt
  public void saveToJenaWithRdfConnection(Model model) {
    log.info("Saving model to Jena triple store");

    try (RDFConnection conn = RDFConnectionRemote.service(fusekiResourceEndpoint).build()) {
      conn.begin(ReadWrite.WRITE);
      try {
        // Convert the model into a string object in N-Triples format
        StringWriter writer = new StringWriter();
        model.write(writer, "N-TRIPLE");
        String nTriples = writer.toString();

        String sparqlUpdate = String.format(
            "INSERT DATA { %s }",
            nTriples);

        // Execute the insert query
        conn.update(sparqlUpdate);

        conn.commit();
        log.info("Successfully saved model to Jena triple store");
      } catch (Exception e) {
        // If anything goes wrong, rollback the transaction
        conn.abort();
        log.error("Error saving model to Jena triple store: {}. Rolling back transaction.", e.getMessage());
        throw e;
      }
    }

  }

  /**
   * Update Modified Timestamp in the RDF Store.
   */
  public void updateModifiedTimestamp() {
    try (RDFConnection conn = RDFConnectionRemote.service(fusekiResourceEndpoint).build()) {
      conn.begin(ReadWrite.WRITE);
      try {
        // First, delete the existing modified timestamp.
        conn.update(createDeleteModifiedQuery());
        // Second, insert the new modified timestamp.
        conn.update(createInsertModifiedQuery());

        conn.commit();
        log.debug("Successfully updated the modified timestamp in the Jena triple store");
      } catch (Exception e) {
        // If anything goes wrong, rollback the transaction
        conn.abort();
        log.error("Error updating the modified timestamp in the Jena triple store: {}. Rolling back transaction.",
            e.getMessage());
      }
    }
  }

  public String getServiceDescription(RdfFormat format) {
    try {
      log.debug("Creating service description");

      // Create new RDF model
      Model descriptionModel = ModelFactory.createDefaultModel();

      // NameSpace for Service Description
      String sd = "http://www.w3.org/ns/sparql-service-description#";

      // Add Resources to the model
      descriptionModel.createResource()
          .addProperty(RDF.type, descriptionModel.createResource(sd + "Service"))
          .addProperty(
              descriptionModel.createProperty(sd + "endpoint"),
              descriptionModel.createResource(this.serverUrl + "/api/v1/sparql/query"))
          .addProperty(
              descriptionModel.createProperty(sd + "supportedLanguage"),
              descriptionModel.createResource(sd + "SPARQL11Query"))
          // .addProperty(
          // descriptionModel.createProperty(sd + "supportedLanguage"),
          // descriptionModel.createResource(sd + "SPARQL11Update"))
          .addProperty(
              descriptionModel.createProperty(sd + "supportedLanguage"),
              descriptionModel.createResource(sd + "UpdateNotSupported"))
          // .addProperty(
          // descriptionModel.createProperty(sd + "feature"),
          // descriptionModel.createResource(sd + "BasicFederatedQuery"))
          .addProperty(
              descriptionModel.createProperty(sd + "feature"),
              descriptionModel.createResource(sd + "FederatedQueryNotSupported"))
          .addProperty(
              descriptionModel.createProperty(sd + "feature"),
              descriptionModel.createResource(sd + "UnionDefaultGraph"));

      return formatModel(descriptionModel, format);

    } catch (Exception e) {
      log.error("Error creating service description", e);
      throw new RuntimeException("Error creating service description: " + e.getMessage(), e);
    }

  }

  public Optional<String> getModifiedTimestamp() {
    String queryString = "PREFIX dcterms: <http://purl.org/dc/terms/> " +
        "SELECT ?modified WHERE { " +
        "  <http://example.com/resource/metadata> dcterms:modified ?modified . " +
        "}";

    try (QueryExecutionHTTP qexec = QueryExecutionHTTP.service(fusekiResourceEndpoint, queryString)) {
      ResultSet results = qexec.execSelect();
      if (results.hasNext()) {
        String modified = results.next().getLiteral("modified").getString();
        return Optional.of(modified);
      } else {
        return Optional.empty();
      }
    } catch (Exception e) {
      log.error("Error executing query to get modified timestamp: {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  /**
   * The methods below convert the different types of objects returned by each
   * query type
   * into String objects according to the specified RdfFormat.
   */
  private String formatSelectResults(ResultSet results, RdfFormat format) {
    log.debug("Formatting SELECT results to format: {}", format);
    try {
      switch (format) {
        case JSON:
          log.debug("Formatting as JSON");
          ByteArrayOutputStream jsonOutputStream = new ByteArrayOutputStream();
          ResultSetFormatter.outputAsJSON(jsonOutputStream, results);
          return jsonOutputStream.toString();
        case JSONLD:
          log.debug("Formatting as JSON-LD");
          ByteArrayOutputStream jsonldOutputStream = new ByteArrayOutputStream();
          ResultSetFormatter.outputAsJSON(jsonldOutputStream, results);
          return jsonldOutputStream.toString();
        case RDFXML:
          log.debug("Formatting as RDF/XML");
          ByteArrayOutputStream rdfXmlOutputStream = new ByteArrayOutputStream();
          ResultSetFormatter.outputAsXML(rdfXmlOutputStream, results);
          return rdfXmlOutputStream.toString();
        default:
          log.debug("Using default text format");
          return ResultSetFormatter.asText(results);
      }
    } catch (Exception e) {
      log.error("Error formatting SELECT results: {}", e.getMessage(), e);
      throw e;
    }
  }

  private String formatModel(Model model, RdfFormat format) {
    log.debug("Formatting Model to format: {}", format);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      switch (format) {
        case JSON:
          log.debug("Writing model as JSON-LD");
          RDFDataMgr.write(outputStream, model, RDFFormat.JSONLD);
          break;
        case JSONLD:
          log.debug("Writing model as JSON-LD");
          RDFDataMgr.write(outputStream, model, RDFFormat.JSONLD);
          break;
        case RDFXML:
          log.debug("Writing model as RDF/XML");
          RDFDataMgr.write(outputStream, model, RDFFormat.RDFXML);
          break;
        case TURTLE:
          log.debug("Writing model as Turtle");
          RDFDataMgr.write(outputStream, model, RDFFormat.TURTLE);
          break;
        case NTRIPLES:
          log.debug("Writing model as N-Triples");
          RDFDataMgr.write(outputStream, model, RDFFormat.NTRIPLES);
          break;
        default:
          log.debug("Using default JSON-LD format");
          RDFDataMgr.write(outputStream, model, RDFFormat.JSONLD);
      }
      return outputStream.toString();
    } catch (Exception e) {
      log.error("Error formatting Model: {}", e.getMessage(), e);
      throw e;
    }
  }

  private String formatAskResult(boolean result, RdfFormat format) {
    log.debug("Formatting ASK result to format: {}", format);
    try {
      switch (format) {
        case JSON:
        case JSONLD:
          log.debug("Formatting as JSON");
          return String.format("{\"result\": %b}", result);
        default:
          log.debug("Using default string format");
          return Boolean.toString(result);
      }
    } catch (Exception e) {
      log.error("Error formatting ASK result: {}", e.getMessage(), e);
      throw e;
    }
  }

  private String createDeleteModifiedQuery() {
    return "PREFIX dcterms: <http://purl.org/dc/terms/> " +
        "DELETE WHERE { " +
        "  <http://example.com/resource/metadata> dcterms:modified ?modified . " +
        "}";
  }

  private String createInsertModifiedQuery() {
    String currentDateTime = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
    log.debug("Current DateTime = {}", currentDateTime);

    return String.format(
        "PREFIX dcterms: <http://purl.org/dc/terms/> " +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
            "INSERT DATA { " +
            "  <http://example.com/resource/metadata> dcterms:modified \"%s\"^^xsd:dateTime . " +
            "}",
        currentDateTime);
  }
}