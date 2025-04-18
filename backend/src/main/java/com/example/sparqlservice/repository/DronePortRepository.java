package com.example.sparqlservice.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.example.sparqlservice.annotation.UpdateModifiedAt;

import java.io.StringWriter;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;

import io.micrometer.common.lang.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class DronePortRepository {
  private final String fusekiResourceEndpoint;

  public DronePortRepository(@Value("${fuseki.resource-endpoint}") String fusekiResourceEndpoint) {
    this.fusekiResourceEndpoint = fusekiResourceEndpoint;
  }

  @UpdateModifiedAt
  public void updateByDronePortId(String dronePortId, Model dronePortModel) {
    log.debug("updateByDronePortId");
    try (RDFConnection conn = RDFConnectionRemote.service(fusekiResourceEndpoint).build()) {
      conn.begin(ReadWrite.WRITE);
      try {
        // First, delete the existing DronePort specified by dronePortId
        conn.update(createDeleteDronePortByDronePortIdQuery(dronePortId));

        // Convert the model into a string object in N-Triples format
        StringWriter writer = new StringWriter();
        dronePortModel.write(writer, "N-TRIPLE");
        String nTriples = writer.toString();

        String sparqlUpdate = String.format(
            "INSERT DATA { %s }",
            nTriples);

        // Execute the insert query
        conn.update(sparqlUpdate);
        conn.commit();
        log.info("Successfully updated DronePort instance: dronePortId = {}.", dronePortId);
      } catch (Exception e) {
        conn.abort();
        log.error("Error during update DronePort instance: dronePortId = {} error = {}.\nRolling back transaction.",
            dronePortId,
            e.getMessage());
        throw e;
      }
    }

  }

  @UpdateModifiedAt
  public void deleteDronePortByDronePortId(@NonNull String dronePortId) {
    log.debug("deleteDronePortByDronePortId: dronePortId = {}", dronePortId);

    try (RDFConnection conn = RDFConnectionRemote.service(fusekiResourceEndpoint).build()) {
      conn.begin(ReadWrite.WRITE);
      try {
        conn.update(createDeleteDronePortByDronePortIdQuery(dronePortId));
        conn.commit();
        log.info("Successfully deleted DronePort instance: dronePortId = {}.", dronePortId);
      } catch (Exception e) {
        conn.abort();
        log.error("Error during delete DronePort instance: dronePortId = {} error = {}.\nRolling back transaction.",
            dronePortId,
            e.getMessage());
        throw e;
      }
    }
  }

  private String createDeleteDronePortByDronePortIdQuery(String dronePortId) {
    dronePortId = dronePortId.replaceAll("\"", ""); // Remove all double quotes.
    log.debug("dronePortId = {}", dronePortId);

    // The association with the Airway Section is temporarily commented out
    String query = String.format(
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
            "PREFIX : <http://example.com/drone-port#> " +
            "DELETE { " +
            " ?s ?p ?o . " +
            "} WHERE { " +
            " BIND( \"%s\" AS ?dronePortId ) " +
            " ?dronePort :dronePortId ?dronePortId . " +
            " { " +
            " SELECT ?s (count(?s) as ?c) { " +
            " BIND( \"%s\" AS ?dronePortId ) " +
            " ?dronePort a :DronePort ; " +
            " :dronePortId ?dronePortId . " +
            " ?dronePort ( " +
            ":dronePortId | " +
            ":dronePortName | " +
            ":address | " +
            ":manufacturer | " +
            ":dronePortManufacturerId | " +
            ":visDronePortCompanyId | " +
            ":storedAircraftId | " +
            ":storesAircraft | " +
            ":serialNumber | " +
            ":portType | " +
            ":portTypeName | " +
            ":hasPortType | " +
            ":supportDroneType | " +
            ":portUsageType | " +
            ":portUsageTypeName | " +
            ":hasPortUsageType | " +
            ":activeStatus | " +
            ":dataModelType | " +
            ":dronePortType | " +
            ":dronePortTypeName | " +
            ":hasDronePortType | " +
            ":inactiveTimeFrom | " +
            ":inactiveTimeTo | " +
            ":imageData | " +
            ":updateTime | " +
            ":hasGeometry | " +
            ":latitude | " +
            ":longitude | " +
            ":altitude " +
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
            // " FILTER NOT EXISTS { " +
            // " ?s a :AirwaySection " +
            // " } " +
            "}",
        dronePortId, dronePortId);
    log.debug("DELETE QUERY = {}", query);
    return query;
  }

}
