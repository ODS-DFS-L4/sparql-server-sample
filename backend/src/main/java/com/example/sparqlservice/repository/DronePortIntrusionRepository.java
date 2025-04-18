package com.example.sparqlservice.repository;

import java.io.StringWriter;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.sparqlservice.annotation.UpdateModifiedAt;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class DronePortIntrusionRepository {
  private final String fusekiResourceEndpoint;

  public DronePortIntrusionRepository(@Value("${fuseki.resource-endpoint}") String fusekiResourceEndpoint) {
    this.fusekiResourceEndpoint = fusekiResourceEndpoint;
  }

  @UpdateModifiedAt
  public void updateByDronePortId(@NonNull final String dronePortId, @NonNull final Model dronePortIntrusionModel) {
    log.debug("updateByDronePortId");
    try (RDFConnection conn = RDFConnectionRemote.service(fusekiResourceEndpoint).build()) {
      conn.begin(ReadWrite.WRITE);
      try {
        // First, delete the existing DronePort Intrusion specified by dronePortId
        conn.update(createDeleteDronePortIntrusionByDronePortIdQuery(dronePortId));

        // Convert the model into a string object in N-Triples format
        StringWriter writer = new StringWriter();
        dronePortIntrusionModel.write(writer, "N-TRIPLE");
        String nTriples = writer.toString();

        String sparqlUpdate = String.format(
            "INSERT DATA { %s }",
            nTriples);

        // Execute the insert query
        conn.update(sparqlUpdate);
        conn.commit();
        log.info("Successfully updated DronePort Intrusion instance: dronePortId = {}.", dronePortId);
      } catch (Exception e) {
        conn.abort();
        log.error(
            "Error during update DronePort Intrusion instance: dronePortId = {} error = {}.\nRolling back transaction.",
            dronePortId,
            e.getMessage());
        throw e;
      }
    }
  }

  @UpdateModifiedAt
  public void deleteByDronePortId(@NonNull final String dronePortId) {
    log.debug("Attempting to delete DronePortIntrusion with dronePortId: {}", dronePortId);
    try (RDFConnection conn = RDFConnectionRemote.service(fusekiResourceEndpoint).build()) {
      conn.begin(ReadWrite.WRITE);
      try {
        conn.update(createDeleteDronePortIntrusionByDronePortIdQuery(dronePortId));
        conn.commit();
        log.info("DronePortIntrusion successfully deleted for dronePortId: {}.", dronePortId);
      } catch (Exception e) {
        conn.abort();
        log.error("Error during delete DronePortIntrusion for dronePortId: {}. Error: {}. Transaction rolled back.",
            dronePortId,
            e.getMessage());
        throw e;
      }
    }
  }

  private String createDeleteDronePortIntrusionByDronePortIdQuery(@NonNull String dronePortId) {
    dronePortId = dronePortId.replaceAll("\"", ""); // Remove all double quotes.
    log.debug("dronePortId = {}", dronePortId);

    String query = String.format(
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
            "PREFIX : <http://example.com/drone-port#> " +
            "DELETE { " +
            " ?dronePort :hasIntrusion ?intrusion . " +
            " ?s ?p ?o . " +
            "} WHERE { " +
            " BIND( \"%s\" AS ?dronePortId ) " +
            " ?dronePort :hasIntrusion ?intrusion . " +
            " ?intrusion :dronePortId ?dronePortId . " +
            " { " +
            " SELECT ?s (count(?s) as ?c) { " +
            " BIND( \"%s\" AS ?dronePortId ) " +
            " ?intrusion a :DronePortIntrusion ; " +
            " :dronePortId ?dronePortId . " +
            " ?intrusion ( " +
            ":dronePortId | " +
            ":isIntrusionFor | " +
            ":timestamp | " +
            ":anyDetection | " +
            ":hasIntrusionEvent | " +
            ":isEventOfIntrusion | " +
            ":reportEndpointUrl | " +
            ":objectId | " +
            ":objectType | " +
            ":detectionStatus | " +
            ":hasDetectionStatus | " +
            ":latitude | " +
            ":longitude | " +
            ":hasGeometry " +
            ")* ?s " +
            " OPTIONAL { ?ss ?pp ?s } " +
            " } " +
            " GROUP BY ?s " +
            " } " +
            " FILTER (?c > 0) . " +
            " ?s ?p ?o " +
            " FILTER NOT EXISTS { " +
            " ?s ?p ?o . " +
            " FILTER(?p IN (<http://www.w3.org/2000/01/rdf-schema#domain>, " +
            " <http://www.w3.org/2000/01/rdf-schema#range>, " +
            " <http://www.w3.org/2000/01/rdf-schema#subClassOf>, " +
            " <http://www.w3.org/2000/01/rdf-schema#subPropertyOf>, " +
            " <http://www.w3.org/2000/01/rdf-schema#label>, " +
            " <http://www.w3.org/2000/01/rdf-schema#comment>)) " +
            " } " +
            " FILTER NOT EXISTS { " +
            " ?s a :DronePort " +
            " } " +
            "}",
        dronePortId, dronePortId);

    log.debug("DELETE QUERY = {}", query);
    return query;
  }

}
